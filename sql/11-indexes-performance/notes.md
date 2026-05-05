# 11 - Indexes & Performance

---

## What is an Index?

Like a book's index — instead of reading every page, jump directly to the right one.

**Without index:** Postgres scans ALL rows (Seq Scan) — slow on large tables.
**With index:** Postgres jumps directly to matching rows (Index Scan) — fast.

---

## CREATE / DROP INDEX

```sql
-- create
CREATE INDEX idx_city ON employees(city);

-- drop
DROP INDEX idx_city;
DROP INDEX IF EXISTS idx_city;
DROP INDEX CONCURRENTLY idx_city;  -- non-blocking (for production)
```

> Postgres auto-creates indexes for PRIMARY KEY and UNIQUE constraints.

---

## Index Types

| Type | Syntax | Best For |
|------|--------|----------|
| B-Tree | `CREATE INDEX` (default) | `=`, `<`, `>`, `BETWEEN`, `ORDER BY` |
| Hash | `USING HASH` | `=` only (rarely used) |
| GIN | `USING GIN` | JSONB, arrays, full-text search |
| GiST | `USING GIST` | geometry, ranges, full-text |
| BRIN | `USING BRIN` | huge tables with natural order (timestamps) |

```sql
-- B-Tree (default)
CREATE INDEX idx_salary ON employees(salary);

-- GIN for JSONB
CREATE INDEX idx_data ON events USING GIN(payload);

-- GIN for arrays
CREATE INDEX idx_tags ON products USING GIN(tags);

-- BRIN for timestamps (very small index, great for time-series)
CREATE INDEX idx_created ON logs USING BRIN(created_at);
```

---

## Multi-Column Index

```sql
CREATE INDEX idx_city_dept ON employees(city, department);
```

This index helps queries on:
- `WHERE city = 'Hyd'` (leftmost column)
- `WHERE city = 'Hyd' AND department = 'Eng'` (both columns)

Does NOT help:
- `WHERE department = 'Eng'` (not the leftmost column)

> Leftmost prefix rule: index on (A, B, C) helps A, AB, ABC but not B or C alone.

---

## Unique Index

```sql
CREATE UNIQUE INDEX idx_email ON users(email);

-- now duplicate emails cause an error
-- also speeds up lookups on email
```

---

## Partial Index — Index Only Some Rows

```sql
-- only index active users (much smaller, much faster)
CREATE INDEX idx_active ON users(name) WHERE is_active = true;

-- only benefits queries that include the same WHERE condition
SELECT name FROM users WHERE is_active = true AND name = 'Alice';  -- uses index
SELECT name FROM users WHERE name = 'Alice';  -- does NOT use this index
```

> Partial indexes save disk space and are faster because they're smaller.

---

## Expression Index — Index on a Function Result

```sql
-- index on LOWER(email) for case-insensitive search
CREATE INDEX idx_lower_email ON users(LOWER(email));

-- this query now uses the index:
SELECT * FROM users WHERE LOWER(email) = 'alice@example.com';

-- index on month extraction
CREATE INDEX idx_month ON orders(DATE_TRUNC('month', created_at));
```

---

## EXPLAIN — See the Query Plan

```sql
-- show plan without running
EXPLAIN SELECT * FROM employees WHERE city = 'Mumbai';

-- show plan AND run it (shows actual time)
EXPLAIN ANALYZE SELECT * FROM employees WHERE city = 'Mumbai';
```

```
-- WITHOUT index:
Seq Scan on employees  (cost=0.00..1234.00 rows=5000 width=64)
  Filter: (city = 'Mumbai')
  Rows Removed by Filter: 95000
  Execution Time: 45.123 ms

-- WITH index:
Index Scan using idx_city on employees  (cost=0.42..8.44 rows=5000 width=64)
  Index Cond: (city = 'Mumbai')
  Execution Time: 0.523 ms
```

> Look for: **Seq Scan** on large tables = probably needs an index.

---

## Scan Types

| Scan Type | Meaning | When Used |
|-----------|---------|-----------|
| Seq Scan | read every row | no index, small table, or most rows match |
| Index Scan | use index to find rows | selective query, few rows match |
| Bitmap Index Scan | index to find pages, then scan pages | moderate selectivity |
| Index Only Scan | answer from index alone (no table) | all needed columns are in the index |

---

## Check Existing Indexes

```sql
-- list indexes on a table
SELECT indexname, indexdef FROM pg_indexes WHERE tablename = 'employees';

-- index sizes
SELECT indexrelname AS index_name,
    pg_size_pretty(pg_relation_size(indexrelid)) AS size
FROM pg_stat_user_indexes WHERE relname = 'employees';

-- table size vs total (with indexes)
SELECT
    pg_size_pretty(pg_relation_size('employees')) AS table_size,
    pg_size_pretty(pg_total_relation_size('employees')) AS total_with_indexes;

-- find UNUSED indexes (candidates for removal)
SELECT indexrelname, idx_scan AS times_used
FROM pg_stat_user_indexes WHERE idx_scan = 0;
```

---

## When NOT to Index

- Small tables (< 1000 rows) — Seq Scan is fine
- Columns with very few unique values (boolean, status with 3 values)
- Tables with heavy INSERT/UPDATE — each index slows down writes
- Columns you never filter or sort on

---

## Index Cheat Sheet

| Scenario | Index Type |
|----------|-----------|
| WHERE city = 'Mumbai' | B-Tree on `city` |
| WHERE price BETWEEN 10 AND 50 | B-Tree on `price` |
| WHERE LOWER(email) = '...' | Expression B-Tree on `LOWER(email)` |
| WHERE payload @> '{"key":"val"}' | GIN on `payload` (JSONB) |
| WHERE 'tag' = ANY(tags) | GIN on `tags` (array) |
| WHERE created_at > '2024-01-01' (huge table) | BRIN on `created_at` |
| WHERE status = 'active' AND name = '...' | Partial B-Tree on `name WHERE status='active'` |

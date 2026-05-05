# 08 - Common Table Expressions (CTEs)

---

## Basic CTE — Named Subquery

```sql
WITH high_earners AS (
    SELECT name, salary, department
    FROM employees
    WHERE salary > 80000
)
SELECT department, COUNT(*) AS count
FROM high_earners
GROUP BY department;
```

> CTE = give a name to a subquery. Makes complex queries readable.

---

## Multiple CTEs in One Query

```sql
WITH
-- CTE 1: totals per person
person_totals AS (
    SELECT salesperson, SUM(amount) AS total FROM sales GROUP BY salesperson
),
-- CTE 2: overall average
avg_total AS (
    SELECT AVG(total) AS avg_sales FROM person_totals
)
-- final query uses BOTH CTEs
SELECT pt.salesperson, pt.total,
    CASE WHEN pt.total > a.avg_sales THEN 'Above' ELSE 'Below' END AS vs_avg
FROM person_totals pt
CROSS JOIN avg_total a;
```

```
 salesperson | total | vs_avg
-------------+-------+-------
 Alice       | 18500 | Above
 Bob         | 16000 | Below
 Diana       | 24500 | Above
```

---

## CTE for Top-N Per Group

```sql
WITH ranked AS (
    SELECT salesperson, month, amount,
           ROW_NUMBER() OVER (PARTITION BY salesperson ORDER BY amount DESC) AS rn
    FROM sales
)
SELECT salesperson, month, amount
FROM ranked WHERE rn = 1;  -- best month per person
```

> This pattern (CTE + ROW_NUMBER + filter) is the standard way to get top-N per group.

---

## Recursive CTE — Hierarchy / Tree Traversal

Structure: **base case** `UNION ALL` **recursive case**

```sql
WITH RECURSIVE org_chart AS (
    -- BASE CASE: start from the top (CEO has no manager)
    SELECT id, name, manager_id, 1 AS level
    FROM employees WHERE manager_id IS NULL

    UNION ALL

    -- RECURSIVE CASE: find children of current level
    SELECT e.id, e.name, e.manager_id, oc.level + 1
    FROM employees e
    JOIN org_chart oc ON e.manager_id = oc.id
)
SELECT REPEAT('  ', level-1) || name AS org_tree, level
FROM org_chart ORDER BY level, name;
```

```
 org_tree          | level
-------------------+------
 CEO               | 1
   CTO             | 2
   CFO             | 2
     Dev Lead      | 3
     Accountant    | 3
       Dev1        | 4
       Dev2        | 4
```

---

## Recursive CTE — Build a Path

```sql
WITH RECURSIVE chain AS (
    SELECT id, name, manager_id, name::TEXT AS path
    FROM employees WHERE manager_id IS NULL

    UNION ALL

    SELECT e.id, e.name, e.manager_id,
           chain.path || ' -> ' || e.name
    FROM employees e
    JOIN chain ON e.manager_id = chain.id
)
SELECT name, path FROM chain;
```

```
 name     | path
----------+---------------------------
 CEO      | CEO
 CTO      | CEO -> CTO
 Dev Lead | CEO -> CTO -> Dev Lead
 Dev1     | CEO -> CTO -> Dev Lead -> Dev1
```

---

## Recursive CTE — Number Series

```sql
-- generate 1 to 10
WITH RECURSIVE nums AS (
    SELECT 1 AS n              -- base case
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 10   -- recursive + termination
)
SELECT n FROM nums;
```

> MUST have a termination condition (`WHERE n < 10`). Without it = infinite loop.

---

## Recursive CTE — Fibonacci

```sql
WITH RECURSIVE fib AS (
    SELECT 1 AS n, 0::BIGINT AS a, 1::BIGINT AS b
    UNION ALL
    SELECT n+1, b, a+b FROM fib WHERE n < 15
)
SELECT n, a AS fibonacci FROM fib;
```

```
 n  | fibonacci
----+----------
  1 | 0
  2 | 1
  3 | 1
  4 | 2
  5 | 3
  6 | 5
  7 | 8
  ...
```

---

## Writable CTE — DML Inside CTE (Postgres Only)

```sql
-- archive + delete in one atomic statement
WITH archived AS (
    DELETE FROM orders WHERE order_date < '2023-01-01'
    RETURNING *
)
INSERT INTO orders_archive SELECT * FROM archived;
```

> Extremely powerful for data migration, audit trails, cleanup scripts.

---

## CTE vs Subquery vs Temp Table

| Feature | CTE | Subquery | Temp Table |
|---------|-----|----------|------------|
| Reusable in query | Yes (by name) | No (inline) | Yes (in session) |
| Readability | Best | Worst for complex | Good |
| Persists after query | No | No | Yes (until session ends) |
| Can be indexed | No | No | Yes |
| Recursive support | Yes | No | No |
| Performance | Same or inlined | Same | Can be faster (indexed) |

```sql
-- CTE approach (readable)
WITH totals AS (
    SELECT dept, SUM(salary) AS total FROM emp GROUP BY dept
)
SELECT * FROM totals WHERE total > 100000;

-- Subquery approach (same result, less readable)
SELECT * FROM (
    SELECT dept, SUM(salary) AS total FROM emp GROUP BY dept
) sub WHERE total > 100000;

-- Temp table approach (persists, can be indexed)
CREATE TEMP TABLE totals AS
    SELECT dept, SUM(salary) AS total FROM emp GROUP BY dept;
CREATE INDEX ON totals(dept);
SELECT * FROM totals WHERE total > 100000;
```

---

## Key Points

- CTE is scoped to a single query (disappears after)
- Multiple CTEs separated by commas, all after one `WITH`
- Recursive CTEs: always `UNION ALL`, always a termination condition
- Postgres 12+ can inline CTEs for optimization. Force materialization with `AS MATERIALIZED`
- Writable CTEs (INSERT/UPDATE/DELETE inside WITH) are Postgres-specific

# 16 - Postgres Extensions

---

## What Are Extensions?

Pre-built modules that add features to Postgres. Most are pre-installed in the Docker `postgres` image.

```sql
-- install an extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- list installed
SELECT extname, extversion FROM pg_extension;

-- list all available
SELECT name, comment FROM pg_available_extensions ORDER BY name;
```

---

## uuid-ossp — UUID Generation

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

SELECT uuid_generate_v4();   -- random UUID: a1b2c3d4-e5f6-...

-- use as primary key
CREATE TABLE api_keys (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    name TEXT
);

-- Postgres 13+ built-in alternative (no extension needed):
SELECT gen_random_uuid();
```

---

## pgcrypto — Hashing & Encryption

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- bcrypt hash a password
SELECT crypt('mypassword', gen_salt('bf'));
-- $2a$06$xyz... (bcrypt hash)

-- verify a password
SELECT * FROM users
WHERE username = 'alice'
  AND password_hash = crypt('mypassword', password_hash);
-- returns row if password matches, empty if wrong

-- SHA256 hash
SELECT encode(digest('hello', 'sha256'), 'hex');

-- random token (32 hex chars)
SELECT encode(gen_random_bytes(16), 'hex');
```

---

## pg_trgm — Fuzzy Text Search (Typo Tolerant)

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- similarity score (0 to 1)
SELECT similarity('Hyderabad', 'Hyderbad');    -- 0.72 (typo detected!)
SELECT similarity('Bangalore', 'Bangalor');    -- 0.71

-- fuzzy search: find closest matches
SELECT name, similarity(name, 'Bangalor') AS score
FROM cities
WHERE similarity(name, 'Bangalor') > 0.3
ORDER BY score DESC;
```

```
 name      | score
-----------+------
 Bangalore | 0.71
 Belgaum   | 0.33
```

```sql
-- GIN index for fast fuzzy search
CREATE INDEX idx_city_trgm ON cities USING GIN(name gin_trgm_ops);

-- % operator: matches above similarity threshold
SELECT * FROM cities WHERE name % 'Hyderbad';
```

> Great for search bars, autocomplete, "did you mean?" features.

---

## citext — Case-Insensitive Text

```sql
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
    email CITEXT UNIQUE
);

INSERT INTO users VALUES ('Alice@Example.COM');
-- INSERT INTO users VALUES ('alice@example.com');  -- ERROR: duplicate!

SELECT * FROM users WHERE email = 'ALICE@EXAMPLE.COM';  -- matches!
```

> Saves you from writing LOWER() everywhere for case-insensitive comparisons.

---

## hstore — Key-Value Pairs

```sql
CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TABLE product_attrs (
    name TEXT,
    attributes HSTORE
);

INSERT INTO product_attrs VALUES
('Phone', 'brand => Samsung, color => black, storage => 128GB');

-- access a key
SELECT attributes -> 'brand' FROM product_attrs;  -- Samsung

-- check key exists
SELECT * FROM product_attrs WHERE attributes ? 'color';

-- all keys / all values
SELECT akeys(attributes), avals(attributes) FROM product_attrs;

-- expand to rows
SELECT key, value FROM each((SELECT attributes FROM product_attrs LIMIT 1));
```

---

## tablefunc — Pivot / Crosstab

```sql
CREATE EXTENSION IF NOT EXISTS tablefunc;

-- sample data
-- salesperson | quarter | amount
-- Alice       | Q1      | 5000
-- Alice       | Q2      | 7000
-- Bob         | Q1      | 4500
-- Bob         | Q2      | 5500

-- pivot rows into columns
SELECT * FROM crosstab(
    'SELECT salesperson, quarter, amount FROM sales ORDER BY 1, 2',
    'SELECT DISTINCT quarter FROM sales ORDER BY 1'
) AS ct(salesperson TEXT, "Q1" NUMERIC, "Q2" NUMERIC, "Q3" NUMERIC, "Q4" NUMERIC);
```

```
 salesperson | Q1   | Q2   | Q3   | Q4
-------------+------+------+------+------
 Alice       | 5000 | 7000 | 6500 | 8000
 Bob         | 4500 | 5500 | 6000 | 7000
```

---

## fuzzystrmatch — Soundex, Levenshtein

```sql
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;

-- Levenshtein: number of edits to transform one string into another
SELECT levenshtein('kitten', 'sitting');   -- 3
SELECT levenshtein('Hyderabad', 'Hyderbad'); -- 1

-- find closest city name
SELECT name, levenshtein(name, 'Bangalor') AS distance
FROM cities ORDER BY distance LIMIT 3;

-- Soundex: phonetic matching
SELECT soundex('Smith'), soundex('Smyth');  -- same code!
```

---

## unaccent — Remove Accents

```sql
CREATE EXTENSION IF NOT EXISTS unaccent;

SELECT unaccent('Cafe resume naive');   -- Cafe resume naive
SELECT unaccent('Munchen');             -- Munchen
```

> Useful for normalizing search input from international users.

---

## pg_stat_statements — Query Performance Tracking

```sql
-- NOTE: needs config change in Docker (see below)
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- top 10 slowest queries
SELECT LEFT(query, 80) AS query,
    calls,
    ROUND(mean_exec_time::NUMERIC, 2) AS avg_ms,
    rows
FROM pg_stat_statements
ORDER BY mean_exec_time DESC LIMIT 10;
```

**Docker setup** — add to docker-compose command:
```yaml
command: postgres -c shared_preload_libraries=pg_stat_statements
```

---

## btree_gist — Needed for EXCLUDE Constraints

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;
-- required for: EXCLUDE USING GIST (room WITH =, during WITH &&)
-- see folder 13-constraints-advanced
```

---

## Docker Quick Setup Summary

Most extensions work with the standard `postgres:16` image. Just `CREATE EXTENSION`.

**Exceptions:**
- **pg_stat_statements**: needs `shared_preload_libraries` config
- **PostGIS**: needs separate image `postgis/postgis:16-3.4`

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_PASSWORD: training123
    command: postgres -c shared_preload_libraries=pg_stat_statements
    ports:
      - "5432:5432"
```

---

## Extension Cheat Sheet

| Extension | Purpose | Key Function |
|-----------|---------|-------------|
| uuid-ossp | UUIDs | `uuid_generate_v4()` |
| pgcrypto | hashing, encryption | `crypt()`, `gen_salt()` |
| pg_trgm | fuzzy search | `similarity()`, `%` operator |
| citext | case-insensitive text | `CITEXT` type |
| hstore | key-value pairs | `->`, `?`, `each()` |
| tablefunc | pivot tables | `crosstab()` |
| fuzzystrmatch | phonetic/edit distance | `levenshtein()`, `soundex()` |
| unaccent | remove accents | `unaccent()` |
| pg_stat_statements | query perf stats | `pg_stat_statements` view |
| btree_gist | EXCLUDE constraints | enables GiST for scalars |

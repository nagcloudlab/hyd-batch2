# 18 - Docker Postgres Setup & Administration

---

## Docker Compose Setup

```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:16
    container_name: pg_training
    restart: unless-stopped
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: trainer
      POSTGRES_PASSWORD: training123
      POSTGRES_DB: training_db
    volumes:
      - pg_data:/var/lib/postgresql/data
      - ./sql:/sql
    command: >
      postgres
        -c shared_preload_libraries=pg_stat_statements
        -c log_statement=all
        -c log_min_duration_statement=100

volumes:
  pg_data:
```

```bash
docker-compose up -d          # start
docker-compose down           # stop
docker-compose logs -f        # view logs
```

---

## Connecting

```bash
# from host machine
psql -h localhost -U trainer -d training_db

# from inside container
docker exec -it pg_training psql -U trainer -d training_db

# run a SQL file
psql -h localhost -U trainer -d training_db -f practice.sql

# inside psql, run mounted file
\i /sql/01-intermediate-querying/practice.sql
```

---

## Essential psql Commands

| Command | Purpose |
|---------|---------|
| `\l` | list all databases |
| `\c dbname` | switch to a database |
| `\dt` | list tables |
| `\dt+` | list tables with sizes |
| `\d tablename` | describe table (columns, types, constraints) |
| `\d+ tablename` | detailed describe |
| `\di` | list indexes |
| `\dv` | list views |
| `\dm` | list materialized views |
| `\df` | list functions |
| `\dn` | list schemas |
| `\du` | list users/roles |
| `\x` | toggle vertical display |
| `\timing` | toggle query timing |
| `\i file.sql` | run a SQL file |
| `\copy` | client-side import/export |
| `\q` | quit psql |

```
training_db=# \dt
         List of relations
 Schema |    Name    | Type  | Owner
--------+------------+-------+---------
 public | employees  | table | trainer
 public | orders     | table | trainer
```

---

## Database Management

```sql
-- create database
CREATE DATABASE student_db;

-- database sizes
SELECT datname, pg_size_pretty(pg_database_size(datname)) AS size
FROM pg_database WHERE datistemplate = false
ORDER BY pg_database_size(datname) DESC;

-- drop database (must not be connected to it)
DROP DATABASE IF EXISTS student_db;
```

```
 datname     | size
-------------+---------
 training_db | 45 MB
 postgres    | 8 MB
```

---

## User / Role Management

```sql
-- create a read-only user
CREATE ROLE student_ro WITH LOGIN PASSWORD 'readonly123';
GRANT CONNECT ON DATABASE training_db TO student_ro;
GRANT USAGE ON SCHEMA public TO student_ro;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO student_ro;

-- make future tables also readable
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO student_ro;

-- list users
SELECT rolname, rolsuper, rolcreatedb, rolcanlogin FROM pg_roles
WHERE rolname NOT LIKE 'pg_%';
```

---

## Table & Database Sizes

```sql
-- all table sizes
SELECT relname AS table_name,
    pg_size_pretty(pg_relation_size(relid)) AS table_size,
    pg_size_pretty(pg_total_relation_size(relid)) AS total_with_indexes
FROM pg_catalog.pg_statio_user_tables
ORDER BY pg_total_relation_size(relid) DESC;
```

```
 table_name | table_size | total_with_indexes
------------+------------+--------------------
 big_table  | 32 MB      | 45 MB
 employees  | 48 kB      | 80 kB
```

```sql
-- single table
SELECT pg_size_pretty(pg_total_relation_size('employees'));
```

---

## Monitor Active Queries

```sql
-- running queries
SELECT pid, state,
    NOW() - query_start AS duration,
    LEFT(query, 80) AS query
FROM pg_stat_activity
WHERE state != 'idle' AND pid != pg_backend_pid()
ORDER BY duration DESC;

-- kill a long-running query
SELECT pg_cancel_backend(12345);      -- graceful cancel
SELECT pg_terminate_backend(12345);   -- force kill
```

---

## Cache Hit Ratio

```sql
-- should be > 99% for a well-tuned database
SELECT
    datname,
    ROUND(100.0 * blks_hit / NULLIF(blks_hit + blks_read, 0), 2) AS cache_hit_pct
FROM pg_stat_database
WHERE datname = current_database();
```

```
 datname     | cache_hit_pct
-------------+--------------
 training_db | 99.87
```

---

## VACUUM & ANALYZE

Postgres never deletes rows in-place. Dead rows pile up and waste space.

```sql
-- VACUUM: reclaim space from dead rows
VACUUM employees;

-- VACUUM + ANALYZE: reclaim space + update query planner stats
VACUUM ANALYZE employees;

-- FULL VACUUM: rewrites entire table (locks it! use rarely)
VACUUM FULL employees;

-- check what needs vacuuming
SELECT relname, n_dead_tup AS dead_rows,
    last_autovacuum, last_autoanalyze
FROM pg_stat_user_tables
WHERE n_dead_tup > 0
ORDER BY n_dead_tup DESC;
```

```
 relname   | dead_rows | last_autovacuum     | last_autoanalyze
-----------+-----------+---------------------+-------------------
 big_table | 5432      | 2024-06-15 03:00:01 | 2024-06-15 03:00:01
 employees | 12        | 2024-06-14 03:00:01 | 2024-06-14 03:00:01
```

> Postgres runs autovacuum automatically. Manual VACUUM is needed after large bulk deletes/updates.

---

## Configuration Settings

```sql
-- check current settings
SHOW work_mem;              -- memory per sort/hash operation
SHOW shared_buffers;        -- main memory cache
SHOW max_connections;       -- max concurrent connections
SHOW statement_timeout;     -- max query time

-- change for current session
SET work_mem = '256MB';
SET statement_timeout = '30s';

-- show non-default settings
SELECT name, setting, unit FROM pg_settings WHERE source != 'default' ORDER BY name;
```

**Recommended Docker tuning** (add to `command:` in docker-compose):
```
-c shared_buffers=256MB
-c work_mem=64MB
-c maintenance_work_mem=128MB
-c effective_cache_size=768MB
-c random_page_cost=1.1
-c log_min_duration_statement=100
```

---

## Useful Admin Queries

```sql
-- Postgres version
SELECT version();

-- uptime
SELECT NOW() - pg_postmaster_start_time() AS uptime;

-- current user and database
SELECT current_user, current_database();

-- find tables WITHOUT primary keys
SELECT t.table_name
FROM information_schema.tables t
LEFT JOIN information_schema.table_constraints tc
    ON t.table_name = tc.table_name AND tc.constraint_type = 'PRIMARY KEY'
WHERE t.table_schema = 'public' AND t.table_type = 'BASE TABLE'
AND tc.constraint_name IS NULL;

-- find unused indexes (candidates for dropping)
SELECT indexrelname, idx_scan AS times_used
FROM pg_stat_user_indexes WHERE idx_scan = 0 AND schemaname = 'public';

-- total database size
SELECT pg_size_pretty(pg_database_size(current_database()));
```

---

## Quick Admin Checklist

| Check | Query / Command |
|-------|----------------|
| DB size | `SELECT pg_size_pretty(pg_database_size('mydb'))` |
| Table sizes | `\dt+` or query `pg_statio_user_tables` |
| Active queries | `SELECT * FROM pg_stat_activity` |
| Slow queries | `pg_stat_statements` extension |
| Cache hit ratio | should be > 99% |
| Dead rows | `SELECT n_dead_tup FROM pg_stat_user_tables` |
| Unused indexes | `idx_scan = 0` in `pg_stat_user_indexes` |
| Backup | `pg_dump -U user -Fc db > backup.dump` |

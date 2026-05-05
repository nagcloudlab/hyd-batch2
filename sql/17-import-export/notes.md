# 17 - Import / Export Data

---

## COPY vs \copy

| Feature | COPY | \copy |
|---------|------|-------|
| Runs on | Server (inside container) | Client (your machine) |
| File location | must be on server | can be on your laptop |
| Permissions | superuser needed | any user |
| Docker-friendly | file must be inside container | works directly from host |

> For Docker setups, **\copy is easier** — runs from your psql on the host.

---

## Export to CSV

```sql
-- server-side (file inside Docker container)
COPY employees TO '/tmp/employees.csv' WITH CSV HEADER;

-- client-side (file on your machine, run from psql)
\copy employees TO '/Users/nag/data/employees.csv' WITH CSV HEADER

-- export specific columns
COPY (SELECT name, salary FROM employees) TO '/tmp/salaries.csv' WITH CSV HEADER;

-- export to screen (good for quick look)
COPY employees TO STDOUT WITH CSV HEADER;
```

---

## Import from CSV

```sql
-- server-side
COPY employees FROM '/tmp/employees.csv' WITH CSV HEADER;

-- client-side (from psql)
\copy employees FROM '/Users/nag/data/employees.csv' WITH CSV HEADER

-- import specific columns (others get defaults)
\copy employees (name, salary) FROM 'salaries.csv' WITH CSV HEADER
```

---

## Docker: Getting Files In/Out

```bash
# Option 1: docker cp
docker cp employees.csv pg_container:/tmp/employees.csv
# then inside psql: COPY employees FROM '/tmp/employees.csv' WITH CSV HEADER;

# Option 2: mount a volume (in docker-compose.yml)
# volumes:
#   - ./data:/data
# then: COPY employees FROM '/data/employees.csv' WITH CSV HEADER;

# Option 3: use \copy from host psql (easiest)
psql -h localhost -U trainer -d mydb
\copy employees FROM 'employees.csv' WITH CSV HEADER
```

---

## COPY Options

```sql
-- custom delimiter (pipe)
COPY employees TO '/tmp/data.psv' WITH DELIMITER '|' CSV HEADER;

-- tab-separated
COPY employees TO '/tmp/data.tsv' WITH DELIMITER E'\t' CSV HEADER;

-- custom NULL representation
COPY employees FROM '/tmp/data.csv' WITH CSV HEADER NULL 'NA';

-- custom quote character
COPY employees TO '/tmp/data.csv' WITH CSV HEADER QUOTE '"' ESCAPE '\';
```

---

## CREATE TABLE AS — Table From Query

```sql
-- create new table from query result
CREATE TABLE engineering_team AS
SELECT name, salary, hire_date
FROM employees
WHERE department = 'Engineering';

-- alternative syntax
SELECT name, salary INTO sales_team FROM employees WHERE department = 'Sales';
```

---

## INSERT INTO ... SELECT — Copy Between Tables

```sql
-- copy rows from one table to another
INSERT INTO archive_table (name, salary, dept)
SELECT name, salary, department FROM employees WHERE hire_date < '2020-01-01';
```

---

## pg_dump — Backup (Run From Shell)

```bash
# dump entire database (plain SQL)
docker exec pg_container pg_dump -U trainer mydb > backup.sql

# dump specific table
docker exec pg_container pg_dump -U trainer -t employees mydb > emp_backup.sql

# dump custom format (compressed, most flexible)
docker exec pg_container pg_dump -U trainer -Fc mydb > backup.dump

# schema only (no data)
docker exec pg_container pg_dump -U trainer --schema-only mydb > schema.sql

# data only (no CREATE statements)
docker exec pg_container pg_dump -U trainer --data-only mydb > data.sql
```

---

## pg_restore — Restore (Run From Shell)

```bash
# restore plain SQL
docker exec -i pg_container psql -U trainer mydb < backup.sql

# restore custom format
docker exec pg_container pg_restore -U trainer -d mydb backup.dump

# restore specific table from custom format
docker exec pg_container pg_restore -U trainer -d mydb -t employees backup.dump
```

---

## Useful pg_dump Flags

| Flag | Purpose |
|------|---------|
| `-Fc` | custom format (compressed) |
| `-Fd` | directory format (parallel dump) |
| `-j 4` | 4 parallel jobs (with -Fd) |
| `--schema-only` | structure only, no data |
| `--data-only` | data only, no structure |
| `--clean` | add DROP before CREATE |
| `--no-owner` | skip ownership info |
| `-t tablename` | specific table only |

---

## Bulk Import Performance Tips

```sql
-- 1. Use COPY, not INSERT (10-100x faster)
COPY big_table FROM '/tmp/data.csv' WITH CSV HEADER;

-- 2. Drop indexes before import, recreate after
DROP INDEX idx_name;
COPY big_table FROM '/tmp/data.csv' WITH CSV HEADER;
CREATE INDEX idx_name ON big_table(name);

-- 3. Use UNLOGGED table for temp imports (no crash recovery, much faster)
CREATE UNLOGGED TABLE fast_import (id INT, value TEXT);
COPY fast_import FROM '/tmp/data.csv' WITH CSV HEADER;
ALTER TABLE fast_import SET LOGGED;  -- convert back after

-- 4. Wrap in transaction
BEGIN;
COPY big_table FROM '/tmp/data.csv' WITH CSV HEADER;
COMMIT;
```

---

## Quick Reference

| Task | Command |
|------|---------|
| Export CSV | `\copy table TO 'file.csv' WITH CSV HEADER` |
| Import CSV | `\copy table FROM 'file.csv' WITH CSV HEADER` |
| Backup DB | `pg_dump -U user -Fc db > backup.dump` |
| Restore DB | `pg_restore -U user -d db backup.dump` |
| Table from query | `CREATE TABLE new AS SELECT ... FROM old` |
| Copy between tables | `INSERT INTO new SELECT ... FROM old` |

-- ============================================
-- 17 - Import / Export Data Practice
-- ============================================

-- ============================================
-- Setup test table
-- ============================================

DROP TABLE IF EXISTS import_test;
CREATE TABLE import_test (
    id SERIAL PRIMARY KEY,
    name TEXT,
    department TEXT,
    salary NUMERIC(10,2),
    hire_date DATE
);

INSERT INTO import_test (name, department, salary, hire_date) VALUES
('Alice', 'Engineering', 95000, '2020-01-15'),
('Bob', 'Sales', 65000, '2021-03-20'),
('Charlie', 'Engineering', 85000, '2019-07-10'),
('Diana', 'HR', 70000, '2022-05-01'),
('Eve', 'Sales', 72000, '2020-11-25');

-- ============================================
-- EXPORT to CSV using COPY (server-side)
-- ============================================

-- export entire table to CSV (file on server / inside Docker container)
-- COPY import_test TO '/tmp/employees.csv' WITH CSV HEADER;

-- export specific columns
-- COPY (SELECT name, salary FROM import_test) TO '/tmp/salaries.csv' WITH CSV HEADER;

-- export with custom delimiter
-- COPY import_test TO '/tmp/employees.tsv' WITH DELIMITER E'\t' CSV HEADER;

-- ============================================
-- EXPORT using \copy (client-side, from psql)
-- ============================================

-- run these from psql prompt (not inside SQL file):
-- \copy import_test TO '/Users/nag/hyd-batch2/sql/17-import-export/employees.csv' WITH CSV HEADER
-- \copy (SELECT name, salary FROM import_test) TO 'salaries.csv' WITH CSV HEADER

-- ============================================
-- IMPORT from CSV using COPY (server-side)
-- ============================================

-- first create the target table
DROP TABLE IF EXISTS imported_data;
CREATE TABLE imported_data (
    id INT,
    name TEXT,
    department TEXT,
    salary NUMERIC(10,2),
    hire_date DATE
);

-- import from CSV (file must be on server / inside Docker container)
-- COPY imported_data FROM '/tmp/employees.csv' WITH CSV HEADER;

-- import specific columns (others get defaults)
-- COPY imported_data (name, salary) FROM '/tmp/salaries.csv' WITH CSV HEADER;

-- ============================================
-- IMPORT using \copy (client-side, from psql)
-- ============================================

-- run from psql:
-- \copy imported_data FROM 'employees.csv' WITH CSV HEADER

-- ============================================
-- Docker-specific: copy file into container then COPY
-- ============================================

-- Step 1: Copy file from host to container
-- docker cp employees.csv <container_name>:/tmp/employees.csv

-- Step 2: Run COPY inside psql
-- COPY imported_data FROM '/tmp/employees.csv' WITH CSV HEADER;

-- OR mount a volume in docker-compose:
-- volumes:
--   - ./data:/data
-- Then: COPY imported_data FROM '/data/employees.csv' WITH CSV HEADER;

-- ============================================
-- CREATE TABLE AS SELECT (table from query)
-- ============================================

-- create a new table from a query result
DROP TABLE IF EXISTS engineering_team;
CREATE TABLE engineering_team AS
SELECT name, salary, hire_date
FROM import_test
WHERE department = 'Engineering';

SELECT * FROM engineering_team;

-- ============================================
-- SELECT INTO (alternative syntax)
-- ============================================

DROP TABLE IF EXISTS sales_team;
SELECT name, salary, hire_date
INTO sales_team
FROM import_test
WHERE department = 'Sales';

SELECT * FROM sales_team;

-- ============================================
-- INSERT INTO ... SELECT (copy between tables)
-- ============================================

DROP TABLE IF EXISTS all_team;
CREATE TABLE all_team (name TEXT, salary NUMERIC, hire_date DATE);

INSERT INTO all_team (name, salary, hire_date)
SELECT name, salary, hire_date FROM import_test
WHERE salary > 70000;

SELECT * FROM all_team;

-- ============================================
-- COPY with options
-- ============================================

-- NULL handling
-- COPY import_test TO '/tmp/test.csv' WITH CSV HEADER NULL 'NA';

-- custom delimiter (pipe)
-- COPY import_test TO '/tmp/test.psv' WITH DELIMITER '|' CSV HEADER;

-- quote character
-- COPY import_test TO '/tmp/test.csv' WITH CSV HEADER QUOTE '"' ESCAPE '\';

-- ============================================
-- pg_dump / pg_restore (run from shell, not SQL)
-- ============================================

-- BACKUP: dump entire database
-- docker exec <container> pg_dump -U postgres mydb > backup.sql

-- BACKUP: dump specific table
-- docker exec <container> pg_dump -U postgres -t import_test mydb > table_backup.sql

-- BACKUP: custom format (compressed, flexible restore)
-- docker exec <container> pg_dump -U postgres -Fc mydb > backup.dump

-- BACKUP: directory format (parallel dump)
-- docker exec <container> pg_dump -U postgres -Fd -j4 mydb -f /tmp/backup_dir

-- RESTORE: from plain SQL
-- docker exec -i <container> psql -U postgres mydb < backup.sql

-- RESTORE: from custom format
-- docker exec <container> pg_restore -U postgres -d mydb backup.dump

-- RESTORE: specific table from custom format
-- docker exec <container> pg_restore -U postgres -d mydb -t import_test backup.dump

-- ============================================
-- Useful pg_dump options
-- ============================================

-- --schema-only    : dump structure only (no data)
-- --data-only      : dump data only (no CREATE statements)
-- --clean          : add DROP statements before CREATE
-- --if-exists      : use IF EXISTS with DROP
-- --no-owner       : don't dump ownership
-- --no-privileges  : don't dump GRANT/REVOKE
-- -Fc              : custom format (compressed)
-- -Fd              : directory format (parallel)
-- -j N             : parallel jobs (with -Fd)

-- ============================================
-- GENERATE sample CSV (using SQL)
-- ============================================

-- generate CSV content you can copy-paste to a file
SELECT 'id,name,department,salary' AS csv
UNION ALL
SELECT id || ',' || name || ',' || department || ',' || salary
FROM import_test;

-- or using COPY TO STDOUT
-- COPY import_test TO STDOUT WITH CSV HEADER;

-- ============================================
-- Bulk insert performance tips
-- ============================================

-- 1. Drop indexes before bulk insert, recreate after
-- 2. Use COPY instead of INSERT (10x+ faster)
-- 3. Use UNLOGGED tables for temp import (no WAL = faster, but no crash recovery)
-- 4. Increase work_mem and maintenance_work_mem for large imports
-- 5. Wrap in a single transaction

-- example: fast bulk import pattern
DROP TABLE IF EXISTS fast_import;
CREATE UNLOGGED TABLE fast_import (id INT, value TEXT);

-- after import, convert to logged:
-- ALTER TABLE fast_import SET LOGGED;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Export the import_test table to CSV format using COPY TO STDOUT
--     (so it prints to screen)

-- Q2: Create a new table "high_earners" from a SELECT of employees
--     earning > 80000

-- Q3: Write the shell commands to backup and restore a database
--     using Docker (as comments)

-- Q4: Insert data from import_test into a new table, but add a
--     "seniority" column calculated from hire_date

-- Q5: Show how you would import a CSV with NULL values represented as 'N/A'

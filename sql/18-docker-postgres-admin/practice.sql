-- ============================================
-- 18 - Docker Postgres Setup & Administration
-- ============================================

-- ============================================
-- DOCKER-COMPOSE SETUP (save as docker-compose.yml)
-- ============================================

/*
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
      - ./sql:/sql                        # mount your SQL files
    command: >
      postgres
        -c shared_preload_libraries=pg_stat_statements
        -c pg_stat_statements.track=all
        -c log_statement=all
        -c log_min_duration_statement=100

volumes:
  pg_data:
*/

-- Start: docker-compose up -d
-- Stop:  docker-compose down
-- Logs:  docker-compose logs -f postgres

-- ============================================
-- CONNECT from host
-- ============================================

-- using psql from host machine:
-- psql -h localhost -U trainer -d training_db

-- using Docker exec:
-- docker exec -it pg_training psql -U trainer -d training_db

-- run a SQL file:
-- docker exec -i pg_training psql -U trainer -d training_db < myfile.sql
-- OR inside psql: \i /sql/myfile.sql

-- ============================================
-- DATABASE MANAGEMENT
-- ============================================

-- create a new database
-- CREATE DATABASE student_db;

-- create with options
-- CREATE DATABASE student_db
--     OWNER trainer
--     ENCODING 'UTF8'
--     LC_COLLATE 'en_US.UTF-8'
--     LC_CTYPE 'en_US.UTF-8';

-- list databases
SELECT datname, pg_size_pretty(pg_database_size(datname)) AS size
FROM pg_database
WHERE datistemplate = false
ORDER BY pg_database_size(datname) DESC;

-- drop database (must not be connected to it)
-- DROP DATABASE IF EXISTS student_db;

-- ============================================
-- USER / ROLE MANAGEMENT
-- ============================================

-- create a role
-- CREATE ROLE student_user WITH LOGIN PASSWORD 'student123';

-- grant permissions
-- GRANT CONNECT ON DATABASE training_db TO student_user;
-- GRANT USAGE ON SCHEMA public TO student_user;
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO student_user;
-- GRANT SELECT, INSERT, UPDATE ON specific_table TO student_user;

-- make future tables accessible
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public
-- GRANT SELECT ON TABLES TO student_user;

-- list roles
SELECT rolname, rolsuper, rolcreatedb, rolcanlogin
FROM pg_roles WHERE rolname NOT LIKE 'pg_%'
ORDER BY rolname;

-- ============================================
-- psql META-COMMANDS (run from psql prompt)
-- ============================================

-- \l            -- list all databases
-- \c dbname     -- switch database
-- \dt           -- list tables
-- \dt+          -- list tables with size info
-- \d tablename  -- describe a table (columns, types, constraints)
-- \d+ tablename -- detailed describe (with storage, description)
-- \di           -- list indexes
-- \dv           -- list views
-- \dm           -- list materialized views
-- \df           -- list functions
-- \dn           -- list schemas
-- \du           -- list users/roles
-- \x            -- toggle expanded (vertical) display
-- \timing       -- toggle showing query execution time
-- \i file.sql   -- execute a SQL file
-- \e            -- open editor
-- \q            -- quit psql

-- ============================================
-- TABLE & DATABASE SIZES
-- ============================================

-- database sizes
SELECT datname, pg_size_pretty(pg_database_size(datname)) AS size
FROM pg_database ORDER BY pg_database_size(datname) DESC;

-- table sizes (including indexes)
SELECT
    relname AS table_name,
    pg_size_pretty(pg_relation_size(relid)) AS table_size,
    pg_size_pretty(pg_total_relation_size(relid)) AS total_size,
    pg_size_pretty(pg_total_relation_size(relid) - pg_relation_size(relid)) AS index_size
FROM pg_catalog.pg_statio_user_tables
ORDER BY pg_total_relation_size(relid) DESC;

-- single table size
-- SELECT pg_size_pretty(pg_total_relation_size('big_table'));

-- ============================================
-- MONITORING: Active Queries
-- ============================================

-- see currently running queries
SELECT
    pid,
    now() - pg_stat_activity.query_start AS duration,
    state,
    LEFT(query, 80) AS query
FROM pg_stat_activity
WHERE state != 'idle'
AND pid != pg_backend_pid()
ORDER BY duration DESC;

-- kill a long-running query (use pid from above)
-- SELECT pg_cancel_backend(<pid>);     -- graceful cancel
-- SELECT pg_terminate_backend(<pid>);  -- force kill

-- ============================================
-- MONITORING: Connection Stats
-- ============================================

SELECT
    datname,
    numbackends AS active_connections,
    xact_commit AS commits,
    xact_rollback AS rollbacks,
    blks_read AS disk_reads,
    blks_hit AS cache_hits,
    ROUND(100.0 * blks_hit / NULLIF(blks_hit + blks_read, 0), 2) AS cache_hit_ratio
FROM pg_stat_database
WHERE datname = current_database();

-- ============================================
-- VACUUM & ANALYZE
-- ============================================

-- VACUUM: reclaims storage from dead tuples (deleted/updated rows)
-- ANALYZE: updates statistics for the query planner

-- manual vacuum on a table
-- VACUUM import_test;

-- vacuum + analyze
-- VACUUM ANALYZE import_test;

-- full vacuum (rewrites entire table, locks it — use rarely)
-- VACUUM FULL import_test;

-- analyze only (update statistics)
-- ANALYZE import_test;

-- check when tables were last vacuumed/analyzed
SELECT
    relname,
    last_vacuum,
    last_autovacuum,
    last_analyze,
    last_autoanalyze,
    n_dead_tup AS dead_rows
FROM pg_stat_user_tables
ORDER BY n_dead_tup DESC;

-- ============================================
-- CONFIGURATION
-- ============================================

-- show current settings
SHOW work_mem;
SHOW shared_buffers;
SHOW max_connections;
SHOW effective_cache_size;
SHOW maintenance_work_mem;

-- change for current session
SET work_mem = '256MB';
SET statement_timeout = '30s';

-- show all non-default settings
SELECT name, setting, unit, source
FROM pg_settings
WHERE source != 'default'
ORDER BY name;

-- recommended tuning for training (add to docker command):
-- shared_buffers = 256MB           (25% of container RAM)
-- work_mem = 64MB                  (per-query sort memory)
-- maintenance_work_mem = 128MB     (for VACUUM, CREATE INDEX)
-- effective_cache_size = 768MB     (75% of container RAM)
-- random_page_cost = 1.1           (for SSD)
-- log_min_duration_statement = 100 (log slow queries > 100ms)

-- ============================================
-- pg_stat_statements (query performance)
-- ============================================

-- requires: shared_preload_libraries = 'pg_stat_statements' in config
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- top 10 slowest queries
SELECT
    LEFT(query, 100) AS query,
    calls,
    ROUND(total_exec_time::NUMERIC, 2) AS total_ms,
    ROUND(mean_exec_time::NUMERIC, 2) AS avg_ms,
    rows
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;

-- top 10 most called queries
SELECT
    LEFT(query, 100) AS query,
    calls,
    ROUND(mean_exec_time::NUMERIC, 2) AS avg_ms
FROM pg_stat_statements
ORDER BY calls DESC
LIMIT 10;

-- ============================================
-- USEFUL ADMIN QUERIES
-- ============================================

-- Postgres version
SELECT version();

-- uptime
SELECT pg_postmaster_start_time(),
       NOW() - pg_postmaster_start_time() AS uptime;

-- current user and database
SELECT current_user, current_database(), inet_server_addr(), inet_server_port();

-- list all schemas
SELECT schema_name FROM information_schema.schemata;

-- count of tables per schema
SELECT schemaname, COUNT(*) AS table_count
FROM pg_tables
GROUP BY schemaname
ORDER BY table_count DESC;

-- find tables without primary keys
SELECT t.table_name
FROM information_schema.tables t
LEFT JOIN information_schema.table_constraints tc
    ON t.table_name = tc.table_name AND tc.constraint_type = 'PRIMARY KEY'
WHERE t.table_schema = 'public'
AND t.table_type = 'BASE TABLE'
AND tc.constraint_name IS NULL;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Write a docker-compose.yml that sets up Postgres with pg_stat_statements

-- Q2: Find the top 5 largest tables in the current database

-- Q3: Check the cache hit ratio for the current database
--     (should be > 99% for a well-tuned DB)

-- Q4: List all tables that have dead rows (need vacuuming)

-- Q5: Create a read-only user that can SELECT from all tables
--     but cannot INSERT, UPDATE, or DELETE

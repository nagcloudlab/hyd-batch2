-- ============================================
-- 11 - Indexes & Performance Practice
-- ============================================

DROP TABLE IF EXISTS big_table;

-- create a large test table
CREATE TABLE big_table (
    id SERIAL PRIMARY KEY,
    name TEXT,
    email TEXT,
    city TEXT,
    status TEXT,
    score INT,
    tags TEXT[],
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- insert 100k rows for testing
INSERT INTO big_table (name, email, city, status, score, tags, metadata, created_at)
SELECT
    'User_' || i,
    'user' || i || '@example.com',
    (ARRAY['Hyderabad','Mumbai','Bangalore','Chennai','Pune'])[1 + (i % 5)],
    (ARRAY['active','inactive','pending'])[1 + (i % 3)],
    (random() * 100)::INT,
    ARRAY[(ARRAY['sql','python','java','react','node'])[1 + (i % 5)]],
    jsonb_build_object('level', (i % 5) + 1, 'verified', i % 2 = 0),
    NOW() - (random() * 365)::INT * INTERVAL '1 day'
FROM generate_series(1, 100000) AS i;

-- ============================================
-- EXPLAIN — see the query plan (without running)
-- ============================================

EXPLAIN SELECT * FROM big_table WHERE city = 'Mumbai';
-- shows "Seq Scan" — scanning all rows (no index)

-- ============================================
-- EXPLAIN ANALYZE — run the query and show actual times
-- ============================================

EXPLAIN ANALYZE SELECT * FROM big_table WHERE city = 'Mumbai';
-- shows actual execution time and row counts

-- ============================================
-- CREATE INDEX (B-Tree — default)
-- ============================================

CREATE INDEX idx_city ON big_table(city);

-- now check the plan again
EXPLAIN ANALYZE SELECT * FROM big_table WHERE city = 'Mumbai';
-- should show "Index Scan" or "Bitmap Index Scan" — much faster!

-- ============================================
-- Multi-column index
-- ============================================

CREATE INDEX idx_city_status ON big_table(city, status);

-- benefits queries filtering on city, or city + status
EXPLAIN ANALYZE SELECT * FROM big_table WHERE city = 'Mumbai' AND status = 'active';

-- does NOT help if you only filter on status (leftmost column rule)
EXPLAIN ANALYZE SELECT * FROM big_table WHERE status = 'active';  -- still seq scan

-- ============================================
-- UNIQUE index
-- ============================================

CREATE UNIQUE INDEX idx_unique_email ON big_table(email);

-- this also enforces uniqueness
-- INSERT INTO big_table (name, email) VALUES ('test', 'user1@example.com'); -- ERROR!

-- ============================================
-- Partial index (index with WHERE)
-- ============================================

-- only index active users — smaller and faster
CREATE INDEX idx_active_users ON big_table(name) WHERE status = 'active';

EXPLAIN ANALYZE SELECT name FROM big_table WHERE status = 'active' AND name = 'User_100';

-- ============================================
-- Expression index (index on function result)
-- ============================================

-- index on lowercase email for case-insensitive search
CREATE INDEX idx_lower_email ON big_table(LOWER(email));

EXPLAIN ANALYZE SELECT * FROM big_table WHERE LOWER(email) = 'user500@example.com';

-- index on date part
CREATE INDEX idx_created_month ON big_table(DATE_TRUNC('month', created_at));

EXPLAIN ANALYZE
SELECT * FROM big_table
WHERE DATE_TRUNC('month', created_at) = '2024-06-01';

-- ============================================
-- GIN index (for arrays and JSONB)
-- ============================================

CREATE INDEX idx_tags_gin ON big_table USING GIN(tags);

-- fast array search
EXPLAIN ANALYZE SELECT * FROM big_table WHERE tags @> ARRAY['python'];

-- GIN index on JSONB
CREATE INDEX idx_metadata_gin ON big_table USING GIN(metadata);

-- fast JSONB queries
EXPLAIN ANALYZE SELECT * FROM big_table WHERE metadata @> '{"verified": true}';
EXPLAIN ANALYZE SELECT * FROM big_table WHERE metadata @> '{"level": 3}';

-- ============================================
-- BRIN index (Block Range INdex — for sorted data)
-- ============================================

-- best for naturally ordered data like timestamps
CREATE INDEX idx_created_brin ON big_table USING BRIN(created_at);

-- very small index, good for time-range queries
EXPLAIN ANALYZE
SELECT * FROM big_table WHERE created_at > NOW() - INTERVAL '30 days';

-- ============================================
-- Check existing indexes
-- ============================================

-- list all indexes on a table
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'big_table';

-- index sizes
SELECT
    indexrelname AS index_name,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM pg_stat_user_indexes
WHERE relname = 'big_table'
ORDER BY pg_relation_size(indexrelid) DESC;

-- table size vs total size (with indexes)
SELECT
    pg_size_pretty(pg_relation_size('big_table')) AS table_size,
    pg_size_pretty(pg_total_relation_size('big_table')) AS total_with_indexes;

-- ============================================
-- DROP INDEX
-- ============================================

-- DROP INDEX idx_city;
-- DROP INDEX IF EXISTS idx_city;
-- DROP INDEX CONCURRENTLY idx_city;  -- non-blocking drop (for production)

-- ============================================
-- Index usage stats
-- ============================================

SELECT
    indexrelname AS index_name,
    idx_scan AS times_used,
    idx_tup_read AS rows_read
FROM pg_stat_user_indexes
WHERE relname = 'big_table'
ORDER BY idx_scan DESC;

-- find unused indexes
SELECT indexrelname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0 AND schemaname = 'public';

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Create an index that would speed up:
--     SELECT * FROM big_table WHERE status = 'pending' AND score > 80

-- Q2: Use EXPLAIN ANALYZE to compare a query before and after adding an index

-- Q3: Create a GIN index on metadata and query for metadata->>'level' = '3'

-- Q4: Find the total size of all indexes on big_table

-- Q5: Create a partial index for high-score active users (score > 90, status = 'active')

-- ============================================
-- 16 - Postgres Extensions Practice
-- ============================================

-- ============================================
-- List available and installed extensions
-- ============================================

-- all extensions available to install
SELECT name, default_version, comment
FROM pg_available_extensions
ORDER BY name
LIMIT 30;

-- currently installed extensions
SELECT extname, extversion FROM pg_extension;

-- ============================================
-- uuid-ossp: UUID generation
-- ============================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

SELECT uuid_generate_v4();  -- random UUID
SELECT uuid_generate_v1();  -- time-based UUID

-- use as default PK
DROP TABLE IF EXISTS api_keys;
CREATE TABLE api_keys (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    name TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO api_keys (name) VALUES ('Production'), ('Staging');
SELECT * FROM api_keys;

-- Postgres 13+ built-in alternative (no extension needed):
SELECT gen_random_uuid();

-- ============================================
-- pgcrypto: hashing and encryption
-- ============================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- hash a password with bcrypt (blowfish)
SELECT crypt('mypassword', gen_salt('bf'));

-- verify a password
DROP TABLE IF EXISTS users_auth;
CREATE TABLE users_auth (
    id SERIAL PRIMARY KEY,
    username TEXT UNIQUE,
    password_hash TEXT
);

-- store hashed password
INSERT INTO users_auth (username, password_hash)
VALUES ('alice', crypt('secret123', gen_salt('bf')));

-- verify login (returns row if password matches)
SELECT * FROM users_auth
WHERE username = 'alice'
AND password_hash = crypt('secret123', password_hash);

-- wrong password returns nothing
SELECT * FROM users_auth
WHERE username = 'alice'
AND password_hash = crypt('wrongpassword', password_hash);

-- SHA256 hash
SELECT digest('hello world', 'sha256');
SELECT encode(digest('hello world', 'sha256'), 'hex') AS sha256_hex;

-- random bytes
SELECT encode(gen_random_bytes(16), 'hex') AS random_token;

-- ============================================
-- pg_trgm: fuzzy text search (trigram matching)
-- ============================================

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- similarity score (0 to 1)
SELECT similarity('PostgreSQL', 'Postgres');     -- ~0.6
SELECT similarity('hyderabad', 'hyderabd');       -- high (typo-tolerant)

-- find similar words
SELECT word, similarity(word, 'Bangalre') AS score
FROM (VALUES ('Bangalore'), ('Hyderabad'), ('Mumbai'), ('Belgaum')) AS t(word)
WHERE similarity(word, 'Bangalre') > 0.3
ORDER BY score DESC;

-- fuzzy search on a table
DROP TABLE IF EXISTS cities;
CREATE TABLE cities (id SERIAL, name TEXT);
INSERT INTO cities (name) VALUES
('Hyderabad'), ('Bangalore'), ('Mumbai'), ('Chennai'), ('Kolkata'),
('Ahmedabad'), ('Pune'), ('Jaipur'), ('Lucknow'), ('Bhopal');

-- GIN index for fast trigram search
CREATE INDEX idx_cities_trgm ON cities USING GIN(name gin_trgm_ops);

-- search with typo tolerance
SELECT * FROM cities WHERE name % 'Bangalor';      -- % = similarity > threshold
SELECT * FROM cities WHERE name ILIKE '%abad%';     -- also faster with trgm index

-- set similarity threshold
SET pg_trgm.similarity_threshold = 0.3;
SELECT * FROM cities WHERE name % 'Hyderbad';

-- ============================================
-- citext: case-insensitive text
-- ============================================

CREATE EXTENSION IF NOT EXISTS citext;

DROP TABLE IF EXISTS users_ci;
CREATE TABLE users_ci (
    id SERIAL PRIMARY KEY,
    email CITEXT UNIQUE  -- case insensitive!
);

INSERT INTO users_ci (email) VALUES ('Alice@Example.COM');
-- this will FAIL (duplicate, case insensitive)
-- INSERT INTO users_ci (email) VALUES ('alice@example.com');

SELECT * FROM users_ci WHERE email = 'ALICE@EXAMPLE.COM';  -- matches!

-- ============================================
-- hstore: key-value pairs
-- ============================================

CREATE EXTENSION IF NOT EXISTS hstore;

DROP TABLE IF EXISTS product_attrs;
CREATE TABLE product_attrs (
    id SERIAL PRIMARY KEY,
    name TEXT,
    attributes HSTORE
);

INSERT INTO product_attrs (name, attributes) VALUES
('Phone', 'brand => Samsung, color => black, storage => 128GB'),
('Laptop', 'brand => Dell, ram => 16GB, screen => 15.6inch');

-- access a key
SELECT name, attributes -> 'brand' AS brand FROM product_attrs;

-- check if key exists
SELECT * FROM product_attrs WHERE attributes ? 'ram';

-- get all keys
SELECT name, akeys(attributes) FROM product_attrs;

-- get all values
SELECT name, avals(attributes) FROM product_attrs;

-- convert to rows
SELECT name, key, value
FROM product_attrs, each(attributes) AS kv(key, value);

-- ============================================
-- tablefunc: crosstab / pivot tables
-- ============================================

CREATE EXTENSION IF NOT EXISTS tablefunc;

DROP TABLE IF EXISTS quarterly_sales;
CREATE TABLE quarterly_sales (
    salesperson TEXT,
    quarter TEXT,
    amount NUMERIC
);

INSERT INTO quarterly_sales VALUES
('Alice', 'Q1', 5000), ('Alice', 'Q2', 7000),
('Alice', 'Q3', 6500), ('Alice', 'Q4', 8000),
('Bob', 'Q1', 4500), ('Bob', 'Q2', 5500),
('Bob', 'Q3', 6000), ('Bob', 'Q4', 7000);

-- pivot: rows to columns
SELECT * FROM crosstab(
    'SELECT salesperson, quarter, amount FROM quarterly_sales ORDER BY 1, 2',
    'SELECT DISTINCT quarter FROM quarterly_sales ORDER BY 1'
) AS ct(salesperson TEXT, "Q1" NUMERIC, "Q2" NUMERIC, "Q3" NUMERIC, "Q4" NUMERIC);

-- ============================================
-- fuzzystrmatch: Soundex, Levenshtein
-- ============================================

CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;

-- Soundex (phonetic matching)
SELECT soundex('Smith'), soundex('Smyth');  -- same soundex code
SELECT soundex('Robert'), soundex('Rupert'); -- similar

-- Levenshtein distance (edit distance)
SELECT levenshtein('kitten', 'sitting');  -- 3 edits needed
SELECT levenshtein('Hyderabad', 'Hyderbad'); -- 1 (missing 'a')

-- find closest city name
SELECT name, levenshtein(name, 'Bangalor') AS distance
FROM cities
ORDER BY distance
LIMIT 3;

-- Metaphone (better than Soundex for English)
SELECT metaphone('Smith', 4), metaphone('Smyth', 4);

-- ============================================
-- unaccent: remove accents
-- ============================================

CREATE EXTENSION IF NOT EXISTS unaccent;

SELECT unaccent('Café résumé naïve');  -- Cafe resume naive

-- useful for search normalization
-- CREATE INDEX idx_name_unaccent ON table USING GIN(unaccent(name) gin_trgm_ops);

-- ============================================
-- pg_stat_statements: query performance tracking
-- ============================================

-- NOTE: requires adding to shared_preload_libraries in postgresql.conf
-- In Docker: add to docker-compose.yml command:
--   command: postgres -c shared_preload_libraries=pg_stat_statements

CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- view top queries by total time
SELECT
    query,
    calls,
    ROUND(total_exec_time::NUMERIC, 2) AS total_ms,
    ROUND(mean_exec_time::NUMERIC, 2) AS avg_ms,
    rows
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 10;

-- reset stats
-- SELECT pg_stat_statements_reset();

-- ============================================
-- btree_gist (needed for EXCLUDE constraints)
-- ============================================

CREATE EXTENSION IF NOT EXISTS btree_gist;
-- already used in 13-constraints-advanced for room booking example

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Create a table with UUID primary key and insert 5 rows

-- Q2: Hash the string 'hello123' with SHA256 and bcrypt. Compare the results.

-- Q3: Using pg_trgm, find the 3 closest matches to 'Hydrabadh' from cities table

-- Q4: Using tablefunc crosstab, pivot the quarterly_sales to show
--     salesperson as rows and quarters as columns

-- Q5: Create a user signup system where:
--     - emails are case-insensitive (citext)
--     - passwords are stored as bcrypt hashes (pgcrypto)
--     - each user gets a UUID (uuid-ossp)

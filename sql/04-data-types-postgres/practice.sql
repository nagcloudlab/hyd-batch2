-- ============================================
-- 04 - Postgres Data Types Practice
-- ============================================

-- ============================================
-- SERIAL vs GENERATED ALWAYS AS IDENTITY
-- ============================================

-- old way: SERIAL
DROP TABLE IF EXISTS products_old;
CREATE TABLE products_old (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100)
);

-- modern way: IDENTITY (preferred in Postgres 10+)
DROP TABLE IF EXISTS products;
CREATE TABLE products (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL,
    price NUMERIC(10,2),
    weight REAL,
    is_active BOOLEAN DEFAULT true,
    tags TEXT[],                    -- array of text
    metadata JSONB,                -- binary JSON
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- NUMERIC vs REAL/FLOAT (precision matters!)
-- ============================================

-- REAL loses precision - NEVER use for money
SELECT 0.1::REAL + 0.2::REAL AS float_result;         -- 0.30000001
SELECT 0.1::NUMERIC + 0.2::NUMERIC AS numeric_result;  -- 0.3 (exact)

-- ============================================
-- BOOLEAN
-- ============================================

INSERT INTO products (name, price, is_active)
VALUES ('Widget', 29.99, true);

INSERT INTO products (name, price, is_active)
VALUES ('Gadget', 49.99, false);

-- all true values work
SELECT true, 't'::boolean, 'yes'::boolean, '1'::boolean;

-- filter boolean
SELECT * FROM products WHERE is_active;          -- shorthand for is_active = true
SELECT * FROM products WHERE NOT is_active;      -- shorthand for is_active = false

-- ============================================
-- ARRAY type (Postgres specific)
-- ============================================

-- insert arrays
INSERT INTO products (name, price, tags)
VALUES ('Phone', 999.99, ARRAY['electronics', 'mobile', 'smartphone']);

INSERT INTO products (name, price, tags)
VALUES ('Laptop', 1499.99, '{electronics,computer,portable}');

-- query array contains
SELECT * FROM products WHERE 'electronics' = ANY(tags);

-- array contains all
SELECT * FROM products WHERE tags @> ARRAY['electronics', 'mobile'];

-- array length
SELECT name, array_length(tags, 1) AS tag_count FROM products WHERE tags IS NOT NULL;

-- access array element (1-based!)
SELECT name, tags[1] AS first_tag FROM products WHERE tags IS NOT NULL;

-- unnest array (expand to rows)
SELECT name, UNNEST(tags) AS tag FROM products WHERE tags IS NOT NULL;

-- ============================================
-- ENUM type
-- ============================================

DROP TYPE IF EXISTS mood CASCADE;
CREATE TYPE mood AS ENUM ('happy', 'sad', 'neutral');

DROP TABLE IF EXISTS diary;
CREATE TABLE diary (
    id SERIAL PRIMARY KEY,
    entry_date DATE DEFAULT CURRENT_DATE,
    feeling mood NOT NULL,
    note TEXT
);

INSERT INTO diary (feeling, note) VALUES ('happy', 'Great day!');
INSERT INTO diary (feeling, note) VALUES ('sad', 'Rainy day');
-- INSERT INTO diary (feeling, note) VALUES ('angry', 'Test'); -- ERROR: invalid enum value

SELECT * FROM diary;

-- ============================================
-- UUID type
-- ============================================

-- requires extension for generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS sessions;
CREATE TABLE sessions (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_name TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO sessions (user_name) VALUES ('alice');
INSERT INTO sessions (user_name) VALUES ('bob');
SELECT * FROM sessions;

-- also gen_random_uuid() available in Postgres 13+ without extension
SELECT gen_random_uuid();

-- ============================================
-- INET / CIDR (network types)
-- ============================================

DROP TABLE IF EXISTS access_log;
CREATE TABLE access_log (
    id SERIAL PRIMARY KEY,
    ip_address INET,
    subnet CIDR
);

INSERT INTO access_log (ip_address, subnet) VALUES ('192.168.1.100', '192.168.1.0/24');
INSERT INTO access_log (ip_address, subnet) VALUES ('10.0.0.50', '10.0.0.0/8');

-- check if IP is in subnet
SELECT * FROM access_log WHERE ip_address << '192.168.0.0/16'::cidr;

-- ============================================
-- TYPE CASTING (:: operator)
-- ============================================

-- Postgres uses :: for casting (more concise than CAST)
SELECT '42'::INTEGER;
SELECT 42::TEXT;
SELECT '2024-01-15'::DATE;
SELECT 'true'::BOOLEAN;
SELECT '{"name":"test"}'::JSONB;

-- CAST syntax also works
SELECT CAST('42' AS INTEGER);

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Create a table "students" with:
--     id (identity), name (text), grades (integer array),
--     active (boolean), enrolled_at (timestamptz)

-- Q2: Insert 3 students with different grade arrays

-- Q3: Find students who have grade 90 in their grades array

-- Q4: Show the average of each student's grades (hint: unnest)

-- Q5: Create an enum type 'status' (pending, approved, rejected)
--     and use it in a table

-- ============================================
-- 13 - Advanced Constraints Practice
-- ============================================

-- ============================================
-- DEFAULT values
-- ============================================

DROP TABLE IF EXISTS tasks CASCADE;
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    status TEXT DEFAULT 'pending',
    priority INT DEFAULT 3,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO tasks (title) VALUES ('Fix bug');  -- uses all defaults
INSERT INTO tasks (title, status) VALUES ('Add feature', 'in_progress');
SELECT * FROM tasks;

-- ============================================
-- CHECK constraints
-- ============================================

DROP TABLE IF EXISTS products_c CASCADE;
CREATE TABLE products_c (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    price NUMERIC(10,2) CHECK (price > 0),
    discount NUMERIC(5,2) DEFAULT 0 CHECK (discount >= 0 AND discount <= 100),
    stock INT DEFAULT 0 CHECK (stock >= 0),
    -- multi-column check: sale_price must be less than price
    sale_price NUMERIC(10,2),
    CONSTRAINT valid_sale_price CHECK (sale_price IS NULL OR sale_price < price)
);

INSERT INTO products_c (name, price, stock) VALUES ('Phone', 999, 50);
INSERT INTO products_c (name, price, sale_price) VALUES ('Laptop', 1499, 1299);

-- these will FAIL:
-- INSERT INTO products_c (name, price) VALUES ('Bad', -10);          -- price > 0
-- INSERT INTO products_c (name, price, discount) VALUES ('Bad', 100, 150); -- discount <= 100
-- INSERT INTO products_c (name, price, sale_price) VALUES ('Bad', 100, 200); -- sale < price

-- ============================================
-- ON DELETE CASCADE
-- ============================================

DROP TABLE IF EXISTS order_items_c, orders_c, customers_c CASCADE;

CREATE TABLE customers_c (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE orders_c (
    id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES customers_c(id) ON DELETE CASCADE,
    total NUMERIC(10,2)
);

CREATE TABLE order_items_c (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders_c(id) ON DELETE CASCADE,
    product TEXT,
    qty INT
);

INSERT INTO customers_c (name) VALUES ('Alice'), ('Bob');
INSERT INTO orders_c (customer_id, total) VALUES (1, 500), (1, 300), (2, 700);
INSERT INTO order_items_c (order_id, product, qty) VALUES (1, 'Phone', 1), (1, 'Case', 2), (3, 'Laptop', 1);

-- delete customer Alice — all her orders AND order items are auto-deleted
DELETE FROM customers_c WHERE name = 'Alice';

SELECT * FROM customers_c;     -- Alice gone
SELECT * FROM orders_c;         -- orders 1,2 gone
SELECT * FROM order_items_c;    -- items for order 1 gone

-- ============================================
-- ON DELETE SET NULL
-- ============================================

DROP TABLE IF EXISTS articles, authors CASCADE;

CREATE TABLE authors (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE articles (
    id SERIAL PRIMARY KEY,
    title TEXT,
    author_id INT REFERENCES authors(id) ON DELETE SET NULL
);

INSERT INTO authors (name) VALUES ('Ravi'), ('Priya');
INSERT INTO articles (title, author_id) VALUES ('SQL Basics', 1), ('Python Tips', 2), ('Advanced SQL', 1);

DELETE FROM authors WHERE name = 'Ravi';

SELECT * FROM articles;  -- Ravi's articles still exist, author_id is NULL

-- ============================================
-- ON DELETE RESTRICT (default behavior, explicit)
-- ============================================

DROP TABLE IF EXISTS dept_c, emp_c CASCADE;

CREATE TABLE dept_c (
    id SERIAL PRIMARY KEY,
    name TEXT
);

CREATE TABLE emp_c (
    id SERIAL PRIMARY KEY,
    name TEXT,
    dept_id INT REFERENCES dept_c(id) ON DELETE RESTRICT
);

INSERT INTO dept_c (name) VALUES ('Engineering');
INSERT INTO emp_c (name, dept_id) VALUES ('Alice', 1);

-- this will FAIL because employees reference this department
-- DELETE FROM dept_c WHERE id = 1;
-- ERROR: update or delete violates foreign key constraint

-- ============================================
-- ON UPDATE CASCADE
-- ============================================

DROP TABLE IF EXISTS categories, items CASCADE;

CREATE TABLE categories (
    code VARCHAR(10) PRIMARY KEY,
    name TEXT
);

CREATE TABLE items (
    id SERIAL PRIMARY KEY,
    name TEXT,
    category_code VARCHAR(10) REFERENCES categories(code) ON UPDATE CASCADE
);

INSERT INTO categories VALUES ('ELEC', 'Electronics'), ('CLTH', 'Clothing');
INSERT INTO items (name, category_code) VALUES ('Phone', 'ELEC'), ('Shirt', 'CLTH');

-- update category code — items auto-update
UPDATE categories SET code = 'ELECTRONICS' WHERE code = 'ELEC';
SELECT * FROM items;  -- Phone now has category_code = 'ELECTRONICS'

-- ============================================
-- Adding constraints to existing tables
-- ============================================

DROP TABLE IF EXISTS demo_table;
CREATE TABLE demo_table (id SERIAL PRIMARY KEY, email TEXT, age INT);
INSERT INTO demo_table (email, age) VALUES ('a@b.com', 25), ('c@d.com', 30);

-- add NOT NULL
ALTER TABLE demo_table ALTER COLUMN email SET NOT NULL;

-- add CHECK
ALTER TABLE demo_table ADD CONSTRAINT chk_age CHECK (age >= 18);

-- add UNIQUE
ALTER TABLE demo_table ADD CONSTRAINT uniq_email UNIQUE (email);

-- add DEFAULT
ALTER TABLE demo_table ALTER COLUMN age SET DEFAULT 18;

-- ============================================
-- Dropping constraints
-- ============================================

ALTER TABLE demo_table DROP CONSTRAINT chk_age;
ALTER TABLE demo_table DROP CONSTRAINT uniq_email;
ALTER TABLE demo_table ALTER COLUMN email DROP NOT NULL;
ALTER TABLE demo_table ALTER COLUMN age DROP DEFAULT;

-- ============================================
-- DEFERRABLE constraints (checked at COMMIT)
-- ============================================

DROP TABLE IF EXISTS parent_d, child_d CASCADE;

CREATE TABLE parent_d (id INT PRIMARY KEY);
CREATE TABLE child_d (
    id INT PRIMARY KEY,
    parent_id INT REFERENCES parent_d(id)
        DEFERRABLE INITIALLY DEFERRED  -- checked at COMMIT, not immediately
);

BEGIN;
    -- insert child BEFORE parent — normally would fail!
    INSERT INTO child_d VALUES (1, 100);
    INSERT INTO parent_d VALUES (100);   -- parent inserted before commit
COMMIT;  -- constraint checked here — passes because parent exists

SELECT * FROM child_d;
SELECT * FROM parent_d;

-- ============================================
-- EXCLUDE constraint (Postgres specific — prevent overlaps)
-- ============================================

CREATE EXTENSION IF NOT EXISTS btree_gist;

DROP TABLE IF EXISTS room_bookings;
CREATE TABLE room_bookings (
    id SERIAL PRIMARY KEY,
    room TEXT NOT NULL,
    during TSTZRANGE NOT NULL,
    EXCLUDE USING GIST (room WITH =, during WITH &&)
);

-- book room A from 10am to 12pm
INSERT INTO room_bookings (room, during)
VALUES ('Room A', '[2024-06-01 10:00, 2024-06-01 12:00)');

-- book room A from 1pm to 3pm (OK - no overlap)
INSERT INTO room_bookings (room, during)
VALUES ('Room A', '[2024-06-01 13:00, 2024-06-01 15:00)');

-- this will FAIL — overlaps with first booking
-- INSERT INTO room_bookings (room, during)
-- VALUES ('Room A', '[2024-06-01 11:00, 2024-06-01 13:00)');

-- different room is OK
INSERT INTO room_bookings (room, during)
VALUES ('Room B', '[2024-06-01 11:00, 2024-06-01 13:00)');

SELECT * FROM room_bookings;

-- ============================================
-- List all constraints on a table
-- ============================================

SELECT
    conname AS constraint_name,
    contype AS type,
    -- c = check, f = foreign key, p = primary key, u = unique, x = exclude
    pg_get_constraintdef(oid) AS definition
FROM pg_constraint
WHERE conrelid = 'products_c'::regclass;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Create a table "enrollments" with:
--     student_id FK (CASCADE delete), course_id FK (SET NULL on delete),
--     grade CHECK between 0 and 100, enrolled_at with DEFAULT NOW()

-- Q2: Add a UNIQUE constraint on (student_id, course_id) combination

-- Q3: Create a scheduling table that prevents overlapping time slots
--     for the same resource using EXCLUDE constraint

-- Q4: Show all foreign key constraints in the current database

-- Q5: Demonstrate DEFERRABLE constraint with a circular reference scenario

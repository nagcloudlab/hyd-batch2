-- ============================================
-- 14 - Functions & Triggers Practice (PL/pgSQL)
-- ============================================

-- ============================================
-- Basic SQL Function
-- ============================================

-- simple function: calculate tax
CREATE OR REPLACE FUNCTION calc_tax(price NUMERIC, rate NUMERIC DEFAULT 0.18)
RETURNS NUMERIC AS $$
    SELECT ROUND(price * rate, 2);
$$ LANGUAGE SQL;

SELECT calc_tax(1000);        -- 180.00
SELECT calc_tax(1000, 0.12);  -- 120.00

-- ============================================
-- PL/pgSQL Function with logic
-- ============================================

CREATE OR REPLACE FUNCTION get_grade(score INT)
RETURNS TEXT AS $$
BEGIN
    IF score >= 90 THEN RETURN 'A';
    ELSIF score >= 80 THEN RETURN 'B';
    ELSIF score >= 70 THEN RETURN 'C';
    ELSIF score >= 60 THEN RETURN 'D';
    ELSE RETURN 'F';
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT get_grade(95), get_grade(72), get_grade(45);

-- ============================================
-- Function with DECLARE variables
-- ============================================

CREATE OR REPLACE FUNCTION account_summary(acct_holder TEXT)
RETURNS TEXT AS $$
DECLARE
    total_orders INT;
    total_amount NUMERIC;
    result TEXT;
BEGIN
    SELECT COUNT(*), COALESCE(SUM(total), 0)
    INTO total_orders, total_amount
    FROM orders3
    WHERE customer_id = (SELECT id FROM customers3 WHERE name = acct_holder);

    result := acct_holder || ': ' || total_orders || ' orders, total = ' || total_amount;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- SELECT account_summary('Alice');

-- ============================================
-- Function returning TABLE
-- ============================================

DROP TABLE IF EXISTS emp_fn;
CREATE TABLE emp_fn (id SERIAL PRIMARY KEY, name TEXT, dept TEXT, salary NUMERIC);
INSERT INTO emp_fn (name, dept, salary) VALUES
('Alice', 'Eng', 90000), ('Bob', 'Eng', 80000),
('Charlie', 'HR', 70000), ('Diana', 'HR', 75000);

CREATE OR REPLACE FUNCTION get_dept_employees(dept_name TEXT)
RETURNS TABLE(emp_name TEXT, emp_salary NUMERIC) AS $$
BEGIN
    RETURN QUERY
    SELECT name, salary FROM emp_fn WHERE dept = dept_name ORDER BY salary DESC;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM get_dept_employees('Eng');

-- ============================================
-- Function with LOOP
-- ============================================

CREATE OR REPLACE FUNCTION factorial(n INT)
RETURNS BIGINT AS $$
DECLARE
    result BIGINT := 1;
    i INT;
BEGIN
    FOR i IN 1..n LOOP
        result := result * i;
    END LOOP;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

SELECT factorial(5);  -- 120
SELECT factorial(10); -- 3628800

-- ============================================
-- Function with RAISE
-- ============================================

CREATE OR REPLACE FUNCTION safe_divide(a NUMERIC, b NUMERIC)
RETURNS NUMERIC AS $$
BEGIN
    IF b = 0 THEN
        RAISE EXCEPTION 'Division by zero is not allowed';
    END IF;
    RAISE NOTICE 'Dividing % by %', a, b;  -- debug message
    RETURN ROUND(a / b, 4);
END;
$$ LANGUAGE plpgsql;

SELECT safe_divide(10, 3);
-- SELECT safe_divide(10, 0);  -- ERROR: Division by zero is not allowed

-- ============================================
-- DROP / REPLACE functions
-- ============================================

-- DROP FUNCTION calc_tax(NUMERIC, NUMERIC);
-- DROP FUNCTION IF EXISTS calc_tax(NUMERIC, NUMERIC);

-- ============================================
-- TRIGGERS
-- ============================================

DROP TABLE IF EXISTS products_t, products_audit CASCADE;

CREATE TABLE products_t (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    price NUMERIC(10,2),
    updated_at TIMESTAMPTZ
);

CREATE TABLE products_audit (
    id SERIAL PRIMARY KEY,
    product_id INT,
    old_price NUMERIC(10,2),
    new_price NUMERIC(10,2),
    changed_by TEXT DEFAULT current_user,
    changed_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- BEFORE trigger: auto-set updated_at
-- ============================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;  -- MUST return NEW for BEFORE triggers
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_updated_at
    BEFORE INSERT OR UPDATE ON products_t
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

INSERT INTO products_t (name, price) VALUES ('Phone', 999);
INSERT INTO products_t (name, price) VALUES ('Laptop', 1499);
SELECT * FROM products_t;  -- updated_at is auto-set

-- ============================================
-- AFTER trigger: audit price changes
-- ============================================

CREATE OR REPLACE FUNCTION audit_price_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.price IS DISTINCT FROM NEW.price THEN
        INSERT INTO products_audit (product_id, old_price, new_price)
        VALUES (OLD.id, OLD.price, NEW.price);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_price
    AFTER UPDATE ON products_t
    FOR EACH ROW
    EXECUTE FUNCTION audit_price_change();

UPDATE products_t SET price = 899 WHERE name = 'Phone';
UPDATE products_t SET price = 1299 WHERE name = 'Laptop';

SELECT * FROM products_audit;

-- ============================================
-- BEFORE DELETE trigger: prevent deletion
-- ============================================

CREATE OR REPLACE FUNCTION prevent_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Deletion not allowed on this table. Use soft delete instead.';
    RETURN NULL;  -- never reached, but required
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_no_delete
    BEFORE DELETE ON products_t
    FOR EACH ROW
    EXECUTE FUNCTION prevent_delete();

-- DELETE FROM products_t WHERE id = 1;  -- ERROR: Deletion not allowed

-- ============================================
-- INSTEAD OF trigger (for views)
-- ============================================

-- can make views updatable using INSTEAD OF triggers
-- (useful for views that join multiple tables)

-- ============================================
-- STATEMENT-level trigger (fires once per statement)
-- ============================================

CREATE OR REPLACE FUNCTION log_bulk_operation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE NOTICE 'Bulk % operation on products_t at %', TG_OP, NOW();
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_bulk_log
    AFTER INSERT ON products_t
    FOR EACH STATEMENT
    EXECUTE FUNCTION log_bulk_operation();

-- ============================================
-- List triggers on a table
-- ============================================

SELECT trigger_name, event_manipulation, action_timing
FROM information_schema.triggers
WHERE event_object_table = 'products_t';

-- ============================================
-- Drop trigger
-- ============================================

-- DROP TRIGGER trg_no_delete ON products_t;
-- DROP TRIGGER IF EXISTS trg_no_delete ON products_t;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Create a function that takes a table name and returns row count

-- Q2: Create a trigger that automatically converts name to INITCAP
--     on INSERT into any people-like table

-- Q3: Create an audit trigger that logs ALL changes (INSERT/UPDATE/DELETE)
--     to an audit table with old values, new values, and operation type

-- Q4: Create a function that returns the top N salespersons as a TABLE

-- Q5: Create a trigger that prevents salary from being decreased
--     (raise exception if new salary < old salary)

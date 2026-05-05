-- ============================================
-- 14a - User Defined Functions (UDFs) Practice
-- ============================================

-- Setup
DROP TABLE IF EXISTS students_udf, products_udf, audit_log_udf, employees_udf CASCADE;

CREATE TABLE employees_udf (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    department TEXT,
    salary NUMERIC(10,2),
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE students_udf (
    id SERIAL PRIMARY KEY,
    name TEXT,
    marks INT,
    course TEXT
);

CREATE TABLE products_udf (
    id SERIAL PRIMARY KEY,
    name TEXT,
    price NUMERIC(10,2),
    category TEXT
);

CREATE TABLE audit_log_udf (
    id SERIAL PRIMARY KEY,
    action TEXT,
    detail TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO employees_udf (name, department, salary, is_active) VALUES
('Alice', 'Engineering', 95000, true), ('Bob', 'Engineering', 85000, true),
('Charlie', 'Sales', 65000, true), ('Diana', 'Sales', 72000, true),
('Eve', 'HR', 60000, false), ('Frank', 'HR', 70000, true),
('Grace', 'Engineering', 110000, true), ('Hank', 'Sales', 55000, true);

INSERT INTO students_udf (name, marks, course) VALUES
('Ravi', 92, 'SQL'), ('Priya', 78, 'Python'), ('Amit', 85, 'SQL'),
('Sneha', 45, 'Java'), ('Kiran', 67, 'Python'), ('Meera', 95, 'SQL');

INSERT INTO products_udf (name, price, category) VALUES
('Phone', 999, 'Electronics'), ('Laptop', 1499, 'Electronics'),
('Shirt', 49, 'Clothing'), ('Pants', 79, 'Clothing'),
('Book', 25, 'Education'), ('Pen', 5, 'Education');

-- ============================================
-- 1. SCALAR UDF (SQL language — simplest)
-- ============================================

-- calculate GST
CREATE OR REPLACE FUNCTION calc_gst(price NUMERIC, rate NUMERIC DEFAULT 0.18)
RETURNS NUMERIC AS $$
    SELECT ROUND(price * rate, 2);
$$ LANGUAGE SQL IMMUTABLE;

SELECT name, price, calc_gst(price) AS gst, price + calc_gst(price) AS total
FROM products_udf;

-- use in WHERE
SELECT * FROM products_udf WHERE calc_gst(price) > 50;

-- ============================================
-- 2. SCALAR UDF (PL/pgSQL — with logic)
-- ============================================

CREATE OR REPLACE FUNCTION get_grade(score INT)
RETURNS TEXT AS $$
BEGIN
    IF score IS NULL THEN RETURN 'N/A';
    ELSIF score >= 90 THEN RETURN 'A';
    ELSIF score >= 80 THEN RETURN 'B';
    ELSIF score >= 70 THEN RETURN 'C';
    ELSIF score >= 60 THEN RETURN 'D';
    ELSE RETURN 'F';
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

SELECT name, marks, get_grade(marks) AS grade FROM students_udf;

-- use in GROUP BY
SELECT get_grade(marks) AS grade, COUNT(*) AS student_count
FROM students_udf GROUP BY get_grade(marks) ORDER BY grade;

-- ============================================
-- 3. UDF WITH DECLARE — Variables
-- ============================================

CREATE OR REPLACE FUNCTION dept_report(dept_name TEXT)
RETURNS TEXT AS $$
DECLARE
    emp_count INT;
    avg_sal NUMERIC;
    max_sal NUMERIC;
    result TEXT;
BEGIN
    SELECT COUNT(*), ROUND(AVG(salary), 2), MAX(salary)
    INTO emp_count, avg_sal, max_sal
    FROM employees_udf
    WHERE department = dept_name AND is_active = true;

    IF emp_count = 0 THEN
        RETURN dept_name || ': no active employees';
    END IF;

    result := FORMAT('%s: %s employees | Avg: %s | Max: %s',
                     dept_name, emp_count, avg_sal, max_sal);
    RETURN result;
END;
$$ LANGUAGE plpgsql STABLE;

SELECT dept_report('Engineering');
SELECT dept_report('Sales');
SELECT dept_report('Finance');  -- no employees

-- ============================================
-- 4. TABLE-RETURNING UDF
-- ============================================

CREATE OR REPLACE FUNCTION get_top_earners(n INT DEFAULT 5)
RETURNS TABLE(emp_name TEXT, emp_salary NUMERIC, emp_dept TEXT) AS $$
BEGIN
    RETURN QUERY
    SELECT name, salary, department
    FROM employees_udf
    WHERE is_active = true
    ORDER BY salary DESC
    LIMIT n;
END;
$$ LANGUAGE plpgsql STABLE;

-- use like a table
SELECT * FROM get_top_earners(3);
SELECT * FROM get_top_earners() WHERE emp_dept = 'Engineering';

-- ============================================
-- 5. SETOF UDF — Return rows of existing table type
-- ============================================

CREATE OR REPLACE FUNCTION get_dept_employees(dept TEXT)
RETURNS SETOF employees_udf AS $$
    SELECT * FROM employees_udf WHERE department = dept AND is_active = true;
$$ LANGUAGE SQL STABLE;

SELECT * FROM get_dept_employees('Engineering');
SELECT name, salary FROM get_dept_employees('Sales');

-- ============================================
-- 6. OUT Parameters — Multiple Return Values
-- ============================================

CREATE OR REPLACE FUNCTION get_salary_stats(
    IN dept TEXT,
    OUT emp_count INT,
    OUT min_salary NUMERIC,
    OUT avg_salary NUMERIC,
    OUT max_salary NUMERIC
) AS $$
BEGIN
    SELECT COUNT(*), MIN(salary), ROUND(AVG(salary), 2), MAX(salary)
    INTO emp_count, min_salary, avg_salary, max_salary
    FROM employees_udf
    WHERE department = dept AND is_active = true;
END;
$$ LANGUAGE plpgsql STABLE;

-- returns a single record with 4 fields
SELECT * FROM get_salary_stats('Engineering');
SELECT (get_salary_stats('Sales')).*;

-- ============================================
-- 7. VOID UDF — No Return Value (side effect only)
-- ============================================

CREATE OR REPLACE FUNCTION log_action(p_action TEXT, p_detail TEXT)
RETURNS VOID AS $$
BEGIN
    INSERT INTO audit_log_udf (action, detail) VALUES (p_action, p_detail);
END;
$$ LANGUAGE plpgsql VOLATILE;

SELECT log_action('LOGIN', 'user alice logged in');
SELECT log_action('VIEW', 'viewed products page');
SELECT * FROM audit_log_udf;

-- ============================================
-- 8. DEFAULT Parameters
-- ============================================

CREATE OR REPLACE FUNCTION greet(name TEXT, greeting TEXT DEFAULT 'Hello')
RETURNS TEXT AS $$
BEGIN
    RETURN greeting || ', ' || name || '!';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

SELECT greet('Alice');               -- Hello, Alice!
SELECT greet('Alice', 'Namaste');    -- Namaste, Alice!
SELECT greet('Bob', 'Good Morning');

-- ============================================
-- 9. VARIADIC — Variable Number of Arguments
-- ============================================

CREATE OR REPLACE FUNCTION sum_all(VARIADIC nums NUMERIC[])
RETURNS NUMERIC AS $$
    SELECT COALESCE(SUM(n), 0) FROM UNNEST(nums) AS n;
$$ LANGUAGE SQL IMMUTABLE;

SELECT sum_all(10, 20, 30);           -- 60
SELECT sum_all(1, 2, 3, 4, 5, 6);    -- 21

-- string version: join with separator
CREATE OR REPLACE FUNCTION join_strings(sep TEXT, VARIADIC parts TEXT[])
RETURNS TEXT AS $$
    SELECT array_to_string(parts, sep);
$$ LANGUAGE SQL IMMUTABLE;

SELECT join_strings(' - ', 'Hyd', 'Telangana', 'India');  -- Hyd - Telangana - India

-- ============================================
-- 10. STRICT — Auto NULL on NULL input
-- ============================================

CREATE OR REPLACE FUNCTION safe_discount(price NUMERIC, pct NUMERIC)
RETURNS NUMERIC AS $$
    SELECT ROUND(price * (1 - pct / 100), 2);
$$ LANGUAGE SQL IMMUTABLE STRICT;

SELECT safe_discount(1000, 10);     -- 900.00
SELECT safe_discount(1000, NULL);   -- NULL (no error, auto-handled)
SELECT safe_discount(NULL, 10);     -- NULL

-- ============================================
-- 11. Volatility Categories
-- ============================================

-- IMMUTABLE: pure math, no DB access. Can use in indexes.
CREATE OR REPLACE FUNCTION to_inr(usd NUMERIC)
RETURNS NUMERIC AS $$
    SELECT ROUND(usd * 83.5, 2);
$$ LANGUAGE SQL IMMUTABLE;

SELECT to_inr(100);  -- 8350.00

-- can be used in expression index!
CREATE INDEX idx_price_inr ON products_udf(to_inr(price));

-- STABLE: reads DB, same result within one query
CREATE OR REPLACE FUNCTION get_dept_avg(dept TEXT)
RETURNS NUMERIC AS $$
    SELECT ROUND(AVG(salary), 2) FROM employees_udf WHERE department = dept;
$$ LANGUAGE SQL STABLE;

-- VOLATILE: default, can write to DB or return different values each call
-- log_action above is VOLATILE

-- ============================================
-- 12. DO Block — Anonymous One-Time Code
-- ============================================

DO $$
DECLARE
    total_emp INT;
    active_emp INT;
BEGIN
    SELECT COUNT(*) INTO total_emp FROM employees_udf;
    SELECT COUNT(*) INTO active_emp FROM employees_udf WHERE is_active = true;

    RAISE NOTICE '=== Employee Summary ===';
    RAISE NOTICE 'Total: %', total_emp;
    RAISE NOTICE 'Active: %', active_emp;
    RAISE NOTICE 'Inactive: %', total_emp - active_emp;

    IF active_emp > total_emp * 0.8 THEN
        RAISE NOTICE 'Good retention rate!';
    ELSE
        RAISE WARNING 'Low retention rate!';
    END IF;
END;
$$;

-- ============================================
-- 13. Exception Handling
-- ============================================

CREATE OR REPLACE FUNCTION safe_insert_employee(
    p_name TEXT, p_dept TEXT, p_salary NUMERIC
)
RETURNS TEXT AS $$
BEGIN
    INSERT INTO employees_udf (name, department, salary)
    VALUES (p_name, p_dept, p_salary);
    RETURN 'OK: inserted ' || p_name;
EXCEPTION
    WHEN check_violation THEN
        RETURN 'ERROR: invalid salary for ' || p_name;
    WHEN not_null_violation THEN
        RETURN 'ERROR: name cannot be null';
    WHEN OTHERS THEN
        RETURN 'ERROR: ' || SQLERRM;
END;
$$ LANGUAGE plpgsql;

SELECT safe_insert_employee('Zara', 'Engineering', 88000);
SELECT safe_insert_employee(NULL, 'Sales', 50000);

-- ============================================
-- 14. Loops in UDFs
-- ============================================

-- FOR loop: multiplication table
CREATE OR REPLACE FUNCTION multiplication_table(n INT)
RETURNS TABLE(expression TEXT, result INT) AS $$
BEGIN
    FOR i IN 1..10 LOOP
        expression := n || ' x ' || i;
        result := n * i;
        RETURN NEXT;
    END LOOP;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

SELECT * FROM multiplication_table(7);

-- FOREACH loop over array
CREATE OR REPLACE FUNCTION array_stats(VARIADIC nums INT[])
RETURNS TABLE(stat_name TEXT, stat_value NUMERIC) AS $$
DECLARE
    total INT := 0;
    n INT;
BEGIN
    FOREACH n IN ARRAY nums LOOP
        total := total + n;
    END LOOP;

    stat_name := 'sum'; stat_value := total; RETURN NEXT;
    stat_name := 'count'; stat_value := array_length(nums, 1); RETURN NEXT;
    stat_name := 'avg'; stat_value := ROUND(total::NUMERIC / array_length(nums, 1), 2); RETURN NEXT;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

SELECT * FROM array_stats(10, 20, 30, 40, 50);

-- ============================================
-- 15. SECURITY DEFINER
-- ============================================

-- function runs with OWNER's permissions, not caller's
CREATE OR REPLACE FUNCTION get_employee_count()
RETURNS INT
SECURITY DEFINER AS $$
    SELECT COUNT(*)::INT FROM employees_udf;
$$ LANGUAGE SQL STABLE;

-- even users without SELECT on employees_udf can call this
SELECT get_employee_count();

-- ============================================
-- 16. UDFs in Real-World Patterns
-- ============================================

-- pattern: generate formatted IDs
CREATE OR REPLACE FUNCTION generate_emp_id(dept TEXT, seq INT)
RETURNS TEXT AS $$
    SELECT UPPER(LEFT(dept, 3)) || '-' || LPAD(seq::TEXT, 5, '0');
$$ LANGUAGE SQL IMMUTABLE;

SELECT generate_emp_id('Engineering', 42);  -- ENG-00042
SELECT generate_emp_id('Sales', 7);         -- SAL-00007

-- pattern: business rule validation
CREATE OR REPLACE FUNCTION is_eligible_for_promotion(emp_id INT)
RETURNS BOOLEAN AS $$
DECLARE
    emp employees_udf%ROWTYPE;
BEGIN
    SELECT * INTO emp FROM employees_udf WHERE id = emp_id;

    IF NOT FOUND THEN RETURN false; END IF;
    IF NOT emp.is_active THEN RETURN false; END IF;
    IF emp.salary > 100000 THEN RETURN false; END IF;

    RETURN true;
END;
$$ LANGUAGE plpgsql STABLE;

SELECT id, name, salary, is_eligible_for_promotion(id) AS eligible
FROM employees_udf;

-- pattern: dynamic salary band
CREATE OR REPLACE FUNCTION salary_band(sal NUMERIC)
RETURNS TEXT AS $$
BEGIN
    RETURN CASE
        WHEN sal >= 100000 THEN 'A - Executive'
        WHEN sal >= 75000  THEN 'B - Senior'
        WHEN sal >= 50000  THEN 'C - Mid'
        ELSE 'D - Junior'
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

SELECT name, salary, salary_band(salary) AS band
FROM employees_udf ORDER BY salary DESC;

-- ============================================
-- 17. List / Inspect / Drop Functions
-- ============================================

-- list all custom functions
SELECT routine_name, data_type AS returns, routine_type
FROM information_schema.routines
WHERE routine_schema = 'public'
ORDER BY routine_name;

-- see function source code
SELECT proname, prosrc
FROM pg_proc
WHERE pronamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'public')
AND proname = 'get_grade';

-- drop function (must specify parameter types if overloaded)
-- DROP FUNCTION get_grade(INT);
-- DROP FUNCTION IF EXISTS get_grade(INT);

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Create a scalar UDF `format_currency(amount NUMERIC)`
--     that returns text like '₹1,499.00'
--     Hint: TO_CHAR(amount, 'FM99,999.00')

-- Q2: Create a table-returning UDF `search_employees(keyword TEXT)`
--     that returns employees whose name ILIKE the keyword

-- Q3: Create a UDF with OUT params that returns min, max, avg price
--     per category. Call it for 'Electronics'.

-- Q4: Create a VARIADIC UDF `max_of(VARIADIC nums NUMERIC[])`
--     that returns the largest number

-- Q5: Create a UDF `mask_email(email TEXT)` that returns:
--     'alice@gmail.com' -> 'al****@gmail.com'
--     Hint: LEFT + REPEAT + SPLIT_PART

-- Q6: Create a DO block that loops through all departments,
--     and for each one prints: "Engineering: 3 employees, avg 96666.67"

-- Q7: Create a UDF that takes a student name and returns a JSON object:
--     {"name": "Ravi", "marks": 92, "grade": "A"}
--     Hint: use jsonb_build_object + get_grade

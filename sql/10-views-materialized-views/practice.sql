-- ============================================
-- 10 - Views & Materialized Views Practice
-- ============================================

DROP MATERIALIZED VIEW IF EXISTS mv_monthly_sales;
DROP VIEW IF EXISTS v_employee_summary, v_active_products, v_sales_report;
DROP TABLE IF EXISTS sale_records, emp_records;

CREATE TABLE emp_records (
    id SERIAL PRIMARY KEY,
    name TEXT, department TEXT, salary NUMERIC(10,2), is_active BOOLEAN DEFAULT true
);
CREATE TABLE sale_records (
    id SERIAL PRIMARY KEY,
    emp_id INT REFERENCES emp_records(id),
    sale_date DATE, amount NUMERIC(10,2)
);

INSERT INTO emp_records (name, department, salary, is_active) VALUES
('Alice', 'Engineering', 95000, true), ('Bob', 'Sales', 65000, true),
('Charlie', 'Sales', 70000, true), ('Diana', 'Engineering', 110000, true),
('Eve', 'HR', 60000, false);

INSERT INTO sale_records (emp_id, sale_date, amount) VALUES
(2, '2024-01-15', 5000), (2, '2024-02-20', 7000), (2, '2024-03-10', 6500),
(3, '2024-01-20', 4500), (3, '2024-02-25', 5500), (3, '2024-03-15', 8000);

-- ============================================
-- CREATE VIEW
-- ============================================

-- simple view: active employees only
CREATE VIEW v_active_employees AS
SELECT id, name, department, salary
FROM emp_records
WHERE is_active = true;

-- query the view like a table
SELECT * FROM v_active_employees;
SELECT * FROM v_active_employees WHERE department = 'Engineering';

-- view with joins and calculations
CREATE VIEW v_sales_report AS
SELECT
    e.name AS salesperson,
    COUNT(s.id) AS total_sales,
    COALESCE(SUM(s.amount), 0) AS total_revenue,
    ROUND(COALESCE(AVG(s.amount), 0), 2) AS avg_sale
FROM emp_records e
LEFT JOIN sale_records s ON e.id = s.emp_id
WHERE e.department = 'Sales'
GROUP BY e.name;

SELECT * FROM v_sales_report;

-- ============================================
-- CREATE OR REPLACE VIEW
-- ============================================

-- modify existing view (can add columns at the end)
CREATE OR REPLACE VIEW v_active_employees AS
SELECT id, name, department, salary,
       CASE WHEN salary > 90000 THEN 'Senior' ELSE 'Junior' END AS level
FROM emp_records
WHERE is_active = true;

SELECT * FROM v_active_employees;

-- ============================================
-- View for security (expose limited columns)
-- ============================================

-- hide salary from general users
CREATE VIEW v_employee_directory AS
SELECT id, name, department FROM emp_records WHERE is_active = true;

SELECT * FROM v_employee_directory;

-- ============================================
-- Updatable View (simple views can be updated)
-- ============================================

-- insert through view (view must be on single table, no aggregates)
-- INSERT INTO v_active_employees (name, department, salary)
-- VALUES ('Frank', 'Sales', 72000);

-- update through view
-- UPDATE v_active_employees SET salary = 98000 WHERE name = 'Alice';

-- ============================================
-- DROP VIEW
-- ============================================

-- DROP VIEW v_employee_directory;
-- DROP VIEW IF EXISTS v_employee_directory;  -- no error if doesn't exist

-- ============================================
-- MATERIALIZED VIEW (stores data physically)
-- ============================================

CREATE MATERIALIZED VIEW mv_monthly_sales AS
SELECT
    DATE_TRUNC('month', s.sale_date)::DATE AS month,
    e.name AS salesperson,
    COUNT(*) AS num_sales,
    SUM(s.amount) AS total,
    ROUND(AVG(s.amount), 2) AS avg_amount
FROM sale_records s
JOIN emp_records e ON s.emp_id = e.id
GROUP BY DATE_TRUNC('month', s.sale_date), e.name
ORDER BY month, salesperson;

-- query it (fast — reads stored data)
SELECT * FROM mv_monthly_sales;

-- ============================================
-- REFRESH MATERIALIZED VIEW
-- ============================================

-- add new data
INSERT INTO sale_records (emp_id, sale_date, amount) VALUES (2, '2024-04-05', 9000);

-- mv still shows old data!
SELECT * FROM mv_monthly_sales;

-- refresh to update
REFRESH MATERIALIZED VIEW mv_monthly_sales;

-- now shows new data
SELECT * FROM mv_monthly_sales;

-- ============================================
-- REFRESH CONCURRENTLY (no read lock)
-- ============================================

-- requires a UNIQUE INDEX on the materialized view
CREATE UNIQUE INDEX idx_mv_monthly ON mv_monthly_sales (month, salesperson);

-- now can refresh without blocking readers
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_monthly_sales;

-- ============================================
-- Index on Materialized View
-- ============================================

-- materialized views can have indexes (views cannot!)
CREATE INDEX idx_mv_salesperson ON mv_monthly_sales (salesperson);

-- this makes filtered queries fast
SELECT * FROM mv_monthly_sales WHERE salesperson = 'Bob';

-- ============================================
-- List views in the database
-- ============================================

-- list all views
SELECT table_name, table_type
FROM information_schema.tables
WHERE table_schema = 'public'
AND table_type = 'VIEW';

-- list materialized views
SELECT matviewname FROM pg_matviews WHERE schemaname = 'public';

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Create a view showing each department's headcount and average salary

-- Q2: Create a materialized view of quarterly sales totals

-- Q3: Add an index to the materialized view and query it

-- Q4: Create a view that shows employees who earn above department average

-- Q5: Explain when you would choose a view vs a materialized view
--     (write as SQL comment)

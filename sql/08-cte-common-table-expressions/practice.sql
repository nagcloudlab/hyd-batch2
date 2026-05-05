-- ============================================
-- 08 - Common Table Expressions (CTEs)
-- ============================================

DROP TABLE IF EXISTS emp_hierarchy, sales_data;

CREATE TABLE sales_data (
    id SERIAL PRIMARY KEY,
    salesperson TEXT,
    region TEXT,
    month DATE,
    amount NUMERIC(10,2)
);

INSERT INTO sales_data (salesperson, region, month, amount) VALUES
('Alice', 'North', '2024-01-01', 5000), ('Alice', 'North', '2024-02-01', 7000),
('Alice', 'North', '2024-03-01', 6500), ('Bob', 'South', '2024-01-01', 4500),
('Bob', 'South', '2024-02-01', 5500), ('Bob', 'South', '2024-03-01', 6000),
('Charlie', 'North', '2024-01-01', 3000), ('Charlie', 'North', '2024-02-01', 4000),
('Diana', 'South', '2024-01-01', 8000), ('Diana', 'South', '2024-02-01', 7500),
('Diana', 'South', '2024-03-01', 9000);

CREATE TABLE emp_hierarchy (
    id SERIAL PRIMARY KEY,
    name TEXT,
    manager_id INT,
    salary NUMERIC(10,2)
);

INSERT INTO emp_hierarchy (name, manager_id, salary) VALUES
('CEO', NULL, 200000),
('VP Engineering', 1, 150000),
('VP Sales', 1, 140000),
('Tech Lead', 2, 120000),
('Sales Manager', 3, 100000),
('Dev1', 4, 80000),
('Dev2', 4, 85000),
('Sales Rep1', 5, 60000),
('Sales Rep2', 5, 65000);

-- ============================================
-- Basic CTE
-- ============================================

-- simple CTE: name a subquery for readability
WITH monthly_totals AS (
    SELECT
        region,
        DATE_TRUNC('month', month) AS month,
        SUM(amount) AS total
    FROM sales_data
    GROUP BY region, DATE_TRUNC('month', month)
)
SELECT region, month, total
FROM monthly_totals
WHERE total > 5000
ORDER BY region, month;

-- ============================================
-- Multiple CTEs in one query
-- ============================================

WITH
-- CTE 1: total per salesperson
person_totals AS (
    SELECT salesperson, SUM(amount) AS total_sales
    FROM sales_data
    GROUP BY salesperson
),
-- CTE 2: average across all people
overall_avg AS (
    SELECT AVG(total_sales) AS avg_sales
    FROM person_totals
)
-- use both CTEs
SELECT
    pt.salesperson,
    pt.total_sales,
    oa.avg_sales,
    CASE WHEN pt.total_sales > oa.avg_sales THEN 'Above Avg' ELSE 'Below Avg' END AS performance
FROM person_totals pt
CROSS JOIN overall_avg oa
ORDER BY pt.total_sales DESC;

-- ============================================
-- CTE for deduplication / ranking
-- ============================================

-- find the best month for each salesperson
WITH ranked AS (
    SELECT salesperson, month, amount,
           ROW_NUMBER() OVER (PARTITION BY salesperson ORDER BY amount DESC) AS rn
    FROM sales_data
)
SELECT salesperson, month, amount AS best_month_amount
FROM ranked
WHERE rn = 1;

-- ============================================
-- Recursive CTE: org chart / hierarchy
-- ============================================

-- traverse the employee hierarchy top-down
WITH RECURSIVE org_chart AS (
    -- base case: start with CEO (no manager)
    SELECT id, name, manager_id, salary,
           1 AS level,
           name::TEXT AS path
    FROM emp_hierarchy
    WHERE manager_id IS NULL

    UNION ALL

    -- recursive case: join children to parent
    SELECT e.id, e.name, e.manager_id, e.salary,
           oc.level + 1,
           oc.path || ' -> ' || e.name
    FROM emp_hierarchy e
    JOIN org_chart oc ON e.manager_id = oc.id
)
SELECT
    REPEAT('  ', level - 1) || name AS org_tree,
    level,
    salary,
    path
FROM org_chart
ORDER BY path;

-- ============================================
-- Recursive CTE: number series
-- ============================================

-- generate 1 to 10
WITH RECURSIVE nums AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 10
)
SELECT n FROM nums;

-- fibonacci sequence (first 15 numbers)
WITH RECURSIVE fib AS (
    SELECT 1 AS n, 0::BIGINT AS a, 1::BIGINT AS b
    UNION ALL
    SELECT n + 1, b, a + b FROM fib WHERE n < 15
)
SELECT n, a AS fibonacci FROM fib;

-- ============================================
-- Recursive CTE: total cost under each manager
-- ============================================

WITH RECURSIVE team_cost AS (
    SELECT id, name, manager_id, salary, id AS root_id, name AS root_name
    FROM emp_hierarchy

    UNION ALL

    SELECT e.id, e.name, e.manager_id, e.salary, tc.root_id, tc.root_name
    FROM emp_hierarchy e
    JOIN team_cost tc ON e.manager_id = tc.id
    WHERE tc.id != tc.root_id  -- avoid double counting
)
SELECT root_name AS manager,
       COUNT(*) AS team_size,
       SUM(salary) AS total_team_salary
FROM team_cost
GROUP BY root_id, root_name
ORDER BY total_team_salary DESC;

-- ============================================
-- Writable CTE (DML inside CTE) — Postgres specific
-- ============================================

-- archive and delete old records in one statement
-- (demo only — uncomment to run)
/*
WITH archived AS (
    DELETE FROM sales_data
    WHERE month < '2024-02-01'
    RETURNING *
)
INSERT INTO sales_archive SELECT * FROM archived;
*/

-- move data between tables using writable CTE
-- useful for audit trails, data migration

-- ============================================
-- CTE vs Subquery vs Temp Table
-- ============================================

-- Subquery: inline, can't reuse
SELECT * FROM (
    SELECT salesperson, SUM(amount) AS total FROM sales_data GROUP BY salesperson
) sub WHERE total > 10000;

-- CTE: named, reusable within the query, more readable
WITH totals AS (
    SELECT salesperson, SUM(amount) AS total FROM sales_data GROUP BY salesperson
)
SELECT * FROM totals WHERE total > 10000;

-- Temp Table: persists for the session, has indexes
DROP TABLE IF EXISTS temp_totals;
CREATE TEMP TABLE temp_totals AS
    SELECT salesperson, SUM(amount) AS total FROM sales_data GROUP BY salesperson;
SELECT * FROM temp_totals WHERE total > 10000;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Using a CTE, find the salesperson with the highest single-month sales

-- Q2: Using multiple CTEs, show each region's total and
--     what percentage of grand total it represents

-- Q3: Using recursive CTE, find all employees under VP Engineering
--     (direct and indirect reports)

-- Q4: Generate a date series from 2024-01-01 to 2024-12-31 using recursive CTE

-- Q5: Using a CTE, find months where North region outsold South region

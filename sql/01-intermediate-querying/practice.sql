-- ============================================
-- 01 - Intermediate Querying Practice
-- ============================================

-- Setup: Create sample table
DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    department VARCHAR(50),
    salary NUMERIC(10,2),
    city VARCHAR(50),
    hire_date DATE,
    nickname VARCHAR(50)
);

INSERT INTO employees (name, department, salary, city, hire_date, nickname) VALUES
('Alice Johnson', 'Engineering', 85000, 'Hyderabad', '2020-03-15', 'Ali'),
('Bob Smith', 'Engineering', 92000, 'Bangalore', '2019-07-01', NULL),
('Charlie Brown', 'Marketing', 65000, 'Hyderabad', '2021-01-20', 'Charlie'),
('Diana Prince', 'HR', 70000, 'Mumbai', '2018-11-10', NULL),
('Eve Adams', 'Engineering', 110000, 'Hyderabad', '2017-05-25', 'Evie'),
('Frank Castle', 'Marketing', 60000, 'Bangalore', '2022-02-14', NULL),
('Grace Lee', 'HR', 72000, 'Mumbai', '2020-08-30', 'Gracie'),
('Hank Pym', 'Engineering', 95000, 'Chennai', '2019-12-01', NULL),
('Ivy Chen', 'Marketing', 68000, 'Hyderabad', '2021-06-15', NULL),
('Jack Ryan', 'HR', 75000, 'Chennai', '2023-01-10', 'JR');

-- ============================================
-- DISTINCT
-- ============================================

-- get unique departments
SELECT DISTINCT department FROM employees;

-- get unique city + department combinations
SELECT DISTINCT city, department FROM employees;

-- count of unique cities
SELECT COUNT(DISTINCT city) AS unique_cities FROM employees;

-- ============================================
-- LIKE / ILIKE (pattern matching)
-- ============================================

-- names starting with 'A'
SELECT * FROM employees WHERE name LIKE 'A%';

-- names ending with 'n'
SELECT * FROM employees WHERE name LIKE '%n';

-- names containing 'an' (case sensitive)
SELECT * FROM employees WHERE name LIKE '%an%';

-- ILIKE = case insensitive (Postgres only)
SELECT * FROM employees WHERE name ILIKE '%alice%';

-- _ matches exactly one character
-- names where 3rd character is 'e'
SELECT * FROM employees WHERE name LIKE '__e%';

-- ============================================
-- BETWEEN
-- ============================================

-- salary between 70000 and 95000 (inclusive)
SELECT name, salary FROM employees
WHERE salary BETWEEN 70000 AND 95000;

-- hired between 2020 and 2021
SELECT name, hire_date FROM employees
WHERE hire_date BETWEEN '2020-01-01' AND '2021-12-31';

-- NOT BETWEEN
SELECT name, salary FROM employees
WHERE salary NOT BETWEEN 70000 AND 95000;

-- ============================================
-- IN
-- ============================================

-- employees in specific cities
SELECT * FROM employees
WHERE city IN ('Hyderabad', 'Chennai');

-- NOT IN
SELECT * FROM employees
WHERE department NOT IN ('HR', 'Marketing');

-- ============================================
-- LIMIT / OFFSET (pagination)
-- ============================================

-- first 3 rows
SELECT * FROM employees ORDER BY id LIMIT 3;

-- skip first 3, get next 3 (page 2)
SELECT * FROM employees ORDER BY id LIMIT 3 OFFSET 3;

-- top 3 highest salaries
SELECT name, salary FROM employees
ORDER BY salary DESC LIMIT 3;

-- ============================================
-- CASE WHEN
-- ============================================

-- categorize salary
SELECT name, salary,
    CASE
        WHEN salary >= 100000 THEN 'High'
        WHEN salary >= 75000 THEN 'Medium'
        ELSE 'Low'
    END AS salary_band
FROM employees;

-- CASE in ORDER BY — custom sort order
SELECT name, department FROM employees
ORDER BY
    CASE department
        WHEN 'Engineering' THEN 1
        WHEN 'HR' THEN 2
        WHEN 'Marketing' THEN 3
    END;

-- CASE inside aggregate
SELECT department,
    COUNT(CASE WHEN salary >= 80000 THEN 1 END) AS high_earners,
    COUNT(CASE WHEN salary < 80000 THEN 1 END) AS low_earners
FROM employees
GROUP BY department;

-- ============================================
-- COALESCE
-- ============================================

-- use nickname if available, else name
SELECT COALESCE(nickname, name) AS display_name, department
FROM employees;

-- chain multiple fallbacks
SELECT COALESCE(nickname, 'No Nickname') AS nick FROM employees;

-- ============================================
-- NULLIF
-- ============================================

-- NULLIF(a, b) returns NULL if a = b, else returns a
-- useful to avoid division by zero
SELECT name, NULLIF(department, 'HR') AS dept_or_null FROM employees;

-- ============================================
-- IS NULL / IS NOT NULL
-- ============================================

-- employees without nicknames
SELECT name FROM employees WHERE nickname IS NULL;

-- employees with nicknames
SELECT name, nickname FROM employees WHERE nickname IS NOT NULL;

-- ============================================
-- EXERCISES (Try these yourself)
-- ============================================

-- Q1: Find employees whose name contains 'a' (case insensitive) and salary > 70000

-- Q2: Get page 3 of employees (5 per page), ordered by name

-- Q3: Show each employee with a column "seniority":
--     hired before 2019 = 'Senior', 2019-2021 = 'Mid', after 2021 = 'Junior'

-- Q4: Count employees per city, only cities with more than 1 employee

-- Q5: Display nickname if available, otherwise first word of name

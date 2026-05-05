-- ============================================
-- 05 - Advanced Joins Practice
-- ============================================

-- Setup tables
DROP TABLE IF EXISTS order_items, products2, orders2, customers2, employees2;

CREATE TABLE customers2 (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    city TEXT
);

CREATE TABLE orders2 (
    id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES customers2(id),
    order_date DATE,
    total NUMERIC(10,2)
);

CREATE TABLE products2 (
    id SERIAL PRIMARY KEY,
    name TEXT,
    category TEXT,
    price NUMERIC(10,2)
);

CREATE TABLE employees2 (
    id SERIAL PRIMARY KEY,
    name TEXT,
    manager_id INT REFERENCES employees2(id),
    department TEXT
);

INSERT INTO customers2 (name, city) VALUES
('Alice', 'Hyderabad'), ('Bob', 'Mumbai'), ('Charlie', 'Bangalore'),
('Diana', 'Chennai'), ('Eve', 'Pune');

INSERT INTO orders2 (customer_id, order_date, total) VALUES
(1, '2024-01-10', 500), (1, '2024-02-15', 800),
(2, '2024-01-20', 1200), (3, '2024-03-05', 300),
(NULL, '2024-03-10', 150);  -- orphan order (no customer)

INSERT INTO products2 (name, category, price) VALUES
('Phone', 'Electronics', 999), ('Laptop', 'Electronics', 1499),
('Shirt', 'Clothing', 49), ('Pants', 'Clothing', 79),
('Book', 'Education', 25);

INSERT INTO employees2 (name, manager_id, department) VALUES
('CEO', NULL, 'Executive'),
('CTO', 1, 'Engineering'),
('CFO', 1, 'Finance'),
('Dev Lead', 2, 'Engineering'),
('Dev1', 4, 'Engineering'),
('Dev2', 4, 'Engineering'),
('Accountant', 3, 'Finance');

-- ============================================
-- FULL OUTER JOIN
-- ============================================

-- show all customers and all orders, even unmatched
SELECT c.name AS customer, o.id AS order_id, o.total
FROM customers2 c
FULL OUTER JOIN orders2 o ON c.id = o.customer_id
ORDER BY c.name;

-- find customers with NO orders and orders with NO customer
SELECT c.name AS customer, o.id AS order_id, o.total
FROM customers2 c
FULL OUTER JOIN orders2 o ON c.id = o.customer_id
WHERE c.id IS NULL OR o.customer_id IS NULL;

-- ============================================
-- CROSS JOIN (cartesian product)
-- ============================================

-- every customer x every product = all possible combinations
SELECT c.name AS customer, p.name AS product
FROM customers2 c
CROSS JOIN products2 p
ORDER BY c.name, p.name;

-- practical: generate all month x category combinations for reporting
SELECT m.month, p.category
FROM generate_series(1, 12) AS m(month)
CROSS JOIN (SELECT DISTINCT category FROM products2) AS p
ORDER BY m.month, p.category;

-- ============================================
-- SELF JOIN (table joined to itself)
-- ============================================

-- show each employee with their manager's name
SELECT
    e.name AS employee,
    e.department,
    m.name AS manager
FROM employees2 e
LEFT JOIN employees2 m ON e.manager_id = m.id
ORDER BY e.id;

-- find employees who share the same manager
SELECT
    e1.name AS employee1,
    e2.name AS employee2,
    m.name AS shared_manager
FROM employees2 e1
JOIN employees2 e2 ON e1.manager_id = e2.manager_id AND e1.id < e2.id
JOIN employees2 m ON e1.manager_id = m.id;

-- find the reporting chain (who reports to whom)
-- CEO -> CTO -> Dev Lead -> Dev1
WITH RECURSIVE chain AS (
    SELECT id, name, manager_id, name::TEXT AS path, 1 AS level
    FROM employees2 WHERE manager_id IS NULL
    UNION ALL
    SELECT e.id, e.name, e.manager_id,
           chain.path || ' -> ' || e.name, chain.level + 1
    FROM employees2 e
    JOIN chain ON e.manager_id = chain.id
)
SELECT name, path, level FROM chain ORDER BY level, name;

-- ============================================
-- USING clause (shorthand for ON when column names match)
-- ============================================

-- when both tables have same column name, use USING
DROP TABLE IF EXISTS dept, emp;
CREATE TABLE dept (dept_id INT PRIMARY KEY, dept_name TEXT);
CREATE TABLE emp (emp_id SERIAL, name TEXT, dept_id INT REFERENCES dept(dept_id));

INSERT INTO dept VALUES (1, 'Engineering'), (2, 'HR'), (3, 'Marketing');
INSERT INTO emp (name, dept_id) VALUES ('Alice', 1), ('Bob', 2), ('Charlie', 1);

-- USING is shorter than ON emp.dept_id = dept.dept_id
SELECT e.name, d.dept_name
FROM emp e
JOIN dept d USING (dept_id);

-- ============================================
-- NATURAL JOIN (auto-match same column names)
-- ============================================

-- auto joins on all columns with matching names (dept_id in this case)
SELECT * FROM emp NATURAL JOIN dept;

-- WARNING: NATURAL JOIN is fragile — if you add columns with same name
-- it will silently change join behavior. Avoid in production.

-- ============================================
-- Multiple JOIN conditions
-- ============================================

-- join on multiple conditions
DROP TABLE IF EXISTS prices;
CREATE TABLE prices (
    product_id INT,
    region TEXT,
    price NUMERIC(10,2)
);

INSERT INTO prices VALUES
(1, 'North', 999), (1, 'South', 949),
(2, 'North', 1499), (2, 'South', 1399);

SELECT p.name, pr.region, pr.price
FROM products2 p
JOIN prices pr ON p.id = pr.product_id AND pr.region = 'South';

-- ============================================
-- Non-equi join (join on inequality)
-- ============================================

-- find products cheaper than each other product in same category
SELECT
    p1.name AS product,
    p1.price,
    p2.name AS cheaper_than,
    p2.price AS other_price
FROM products2 p1
JOIN products2 p2 ON p1.category = p2.category
                  AND p1.price < p2.price;

-- salary bands with range join
DROP TABLE IF EXISTS salary_bands;
CREATE TABLE salary_bands (
    band TEXT,
    min_sal NUMERIC,
    max_sal NUMERIC
);

INSERT INTO salary_bands VALUES
('Junior', 0, 50000), ('Mid', 50001, 90000), ('Senior', 90001, 150000);

DROP TABLE IF EXISTS staff;
CREATE TABLE staff (name TEXT, salary NUMERIC);
INSERT INTO staff VALUES ('A', 40000), ('B', 75000), ('C', 120000);

SELECT s.name, s.salary, sb.band
FROM staff s
JOIN salary_bands sb ON s.salary BETWEEN sb.min_sal AND sb.max_sal;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Find all customers who have NEVER placed an order (use LEFT JOIN)

-- Q2: Show each employee and how many levels below CEO they are

-- Q3: Using CROSS JOIN, generate a multiplication table (1-5 x 1-5)

-- Q4: Find pairs of products in the same category (avoid duplicates)

-- Q5: FULL OUTER JOIN customers and orders,
--     label each row as 'has_order', 'no_order', or 'no_customer'

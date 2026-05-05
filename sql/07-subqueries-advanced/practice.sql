-- ============================================
-- 07 - Advanced Subqueries Practice
-- ============================================

DROP TABLE IF EXISTS order_items3, orders3, products3, customers3;

CREATE TABLE customers3 (
    id SERIAL PRIMARY KEY, name TEXT, city TEXT
);
CREATE TABLE products3 (
    id SERIAL PRIMARY KEY, name TEXT, category TEXT, price NUMERIC(10,2)
);
CREATE TABLE orders3 (
    id SERIAL PRIMARY KEY, customer_id INT REFERENCES customers3(id),
    order_date DATE, total NUMERIC(10,2)
);

INSERT INTO customers3 (name, city) VALUES
('Alice', 'Hyderabad'), ('Bob', 'Mumbai'), ('Charlie', 'Bangalore'),
('Diana', 'Chennai'), ('Eve', 'Pune');

INSERT INTO products3 (name, category, price) VALUES
('Phone', 'Electronics', 999), ('Laptop', 'Electronics', 1499),
('Shirt', 'Clothing', 49), ('Pants', 'Clothing', 79), ('Book', 'Books', 25);

INSERT INTO orders3 (customer_id, order_date, total) VALUES
(1, '2024-01-10', 500), (1, '2024-02-15', 800), (1, '2024-05-20', 1200),
(2, '2024-01-20', 1200), (2, '2024-06-01', 300),
(3, '2024-03-05', 300);
-- Diana and Eve have NO orders

-- ============================================
-- Subquery in WHERE: IN / NOT IN
-- ============================================

-- customers who placed orders
SELECT * FROM customers3
WHERE id IN (SELECT DISTINCT customer_id FROM orders3);

-- customers who NEVER placed orders
SELECT * FROM customers3
WHERE id NOT IN (SELECT DISTINCT customer_id FROM orders3);

-- ============================================
-- Subquery in SELECT (scalar subquery)
-- ============================================

-- each customer with their total order count
SELECT
    name,
    (SELECT COUNT(*) FROM orders3 o WHERE o.customer_id = c.id) AS order_count,
    (SELECT COALESCE(SUM(total), 0) FROM orders3 o WHERE o.customer_id = c.id) AS total_spent
FROM customers3 c;

-- each product with how many times the avg price of its category
SELECT name, price, category,
    ROUND(price / (SELECT AVG(price) FROM products3 p2 WHERE p2.category = p.category), 2)
        AS times_avg
FROM products3 p;

-- ============================================
-- Subquery in FROM (derived table / inline view)
-- ============================================

-- customer order summary as a derived table
SELECT summary.name, summary.total_orders, summary.total_spent
FROM (
    SELECT c.name,
           COUNT(o.id) AS total_orders,
           COALESCE(SUM(o.total), 0) AS total_spent
    FROM customers3 c
    LEFT JOIN orders3 o ON c.id = o.customer_id
    GROUP BY c.name
) AS summary
WHERE summary.total_spent > 500;

-- top category by total price
SELECT * FROM (
    SELECT category, SUM(price) AS total_value,
           RANK() OVER (ORDER BY SUM(price) DESC) AS rnk
    FROM products3
    GROUP BY category
) ranked
WHERE rnk = 1;

-- ============================================
-- EXISTS / NOT EXISTS
-- ============================================

-- customers who have at least one order (EXISTS)
-- EXISTS stops at first match — faster than IN for large datasets
SELECT c.name
FROM customers3 c
WHERE EXISTS (
    SELECT 1 FROM orders3 o WHERE o.customer_id = c.id
);

-- customers with NO orders (NOT EXISTS)
SELECT c.name
FROM customers3 c
WHERE NOT EXISTS (
    SELECT 1 FROM orders3 o WHERE o.customer_id = c.id
);

-- ============================================
-- ANY / ALL operators
-- ============================================

-- salary greater than ANY value in the subquery (= at least one)
-- products more expensive than ANY clothing item
SELECT * FROM products3
WHERE price > ANY (SELECT price FROM products3 WHERE category = 'Clothing');

-- products more expensive than ALL clothing items
SELECT * FROM products3
WHERE price > ALL (SELECT price FROM products3 WHERE category = 'Clothing');

-- same as: price > (SELECT MAX(price) FROM products3 WHERE category = 'Clothing')

-- ============================================
-- Correlated Subquery
-- ============================================

-- for each customer, find their most recent order
SELECT c.name,
    (SELECT MAX(order_date) FROM orders3 o WHERE o.customer_id = c.id) AS last_order
FROM customers3 c;

-- find orders that are above the average for that specific customer
SELECT o.id, c.name, o.total
FROM orders3 o
JOIN customers3 c ON o.customer_id = c.id
WHERE o.total > (
    SELECT AVG(total) FROM orders3 o2 WHERE o2.customer_id = o.customer_id
);

-- ============================================
-- LATERAL JOIN (Postgres specific)
-- ============================================

-- for each customer, get their latest 2 orders
-- LATERAL lets subquery reference the outer table
SELECT c.name, latest.order_date, latest.total
FROM customers3 c
LEFT JOIN LATERAL (
    SELECT order_date, total
    FROM orders3 o
    WHERE o.customer_id = c.id
    ORDER BY order_date DESC
    LIMIT 2
) latest ON true
ORDER BY c.name, latest.order_date DESC;

-- for each category, get the most expensive product
SELECT p_top.category, p_top.name, p_top.price
FROM (SELECT DISTINCT category FROM products3) cats
JOIN LATERAL (
    SELECT *
    FROM products3 p
    WHERE p.category = cats.category
    ORDER BY price DESC
    LIMIT 1
) p_top ON true;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Find products that are priced above the overall average price

-- Q2: For each customer, show their name and their order count
--     using a scalar subquery (include customers with 0 orders)

-- Q3: Using EXISTS, find categories that have at least one product over 100

-- Q4: Using a derived table, find the city with the highest total order value

-- Q5: Using LATERAL, for each customer show their single largest order

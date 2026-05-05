-- ============================================
-- 03 - Date & Time Practice (Postgres)
-- ============================================

DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_name VARCHAR(100),
    order_date TIMESTAMP,
    delivery_date DATE,
    amount NUMERIC(10,2)
);

INSERT INTO orders (customer_name, order_date, delivery_date, amount) VALUES
('Alice', '2024-01-15 10:30:00', '2024-01-20', 1500.00),
('Bob', '2024-02-20 14:45:00', '2024-02-25', 2300.00),
('Charlie', '2024-03-10 09:15:00', '2024-03-15', 800.00),
('Alice', '2024-03-25 16:00:00', '2024-03-30', 3200.00),
('Bob', '2024-04-05 11:20:00', '2024-04-10', 1100.00),
('Diana', '2024-06-15 08:00:00', '2024-06-20', 4500.00),
('Eve', '2024-07-01 13:30:00', '2024-07-05', 950.00),
('Alice', '2024-09-10 17:45:00', '2024-09-15', 2100.00),
('Frank', '2024-11-20 10:00:00', '2024-11-25', 1800.00),
('Bob', '2024-12-01 15:30:00', '2024-12-06', 3700.00);

-- ============================================
-- NOW(), CURRENT_DATE, CURRENT_TIME
-- ============================================

SELECT NOW();                  -- current timestamp with timezone
SELECT CURRENT_DATE;           -- today's date only
SELECT CURRENT_TIME;           -- current time only
SELECT CURRENT_TIMESTAMP;      -- same as NOW()

-- ============================================
-- AGE() — difference between two dates
-- ============================================

-- how old is each order?
SELECT customer_name, order_date,
       AGE(NOW(), order_date) AS order_age
FROM orders;

-- difference between delivery and order
SELECT customer_name,
       AGE(delivery_date, order_date::date) AS processing_time
FROM orders;

-- ============================================
-- EXTRACT() — pull parts from a date
-- ============================================

SELECT order_date,
       EXTRACT(YEAR FROM order_date) AS year,
       EXTRACT(MONTH FROM order_date) AS month,
       EXTRACT(DAY FROM order_date) AS day,
       EXTRACT(HOUR FROM order_date) AS hour,
       EXTRACT(DOW FROM order_date) AS day_of_week,   -- 0=Sun, 6=Sat
       EXTRACT(QUARTER FROM order_date) AS quarter,
       EXTRACT(WEEK FROM order_date) AS week_number
FROM orders;

-- ============================================
-- DATE_TRUNC() — truncate to precision
-- ============================================

-- truncate to month (great for monthly reports)
SELECT DATE_TRUNC('month', order_date) AS month,
       SUM(amount) AS monthly_total
FROM orders
GROUP BY DATE_TRUNC('month', order_date)
ORDER BY month;

-- truncate to quarter
SELECT DATE_TRUNC('quarter', order_date) AS quarter,
       COUNT(*) AS order_count,
       SUM(amount) AS quarterly_total
FROM orders
GROUP BY DATE_TRUNC('quarter', order_date)
ORDER BY quarter;

-- truncate to week
SELECT DATE_TRUNC('week', order_date) AS week_start,
       COUNT(*) AS orders_in_week
FROM orders
GROUP BY DATE_TRUNC('week', order_date)
ORDER BY week_start;

-- ============================================
-- TO_CHAR() — format date as string
-- ============================================

SELECT order_date,
       TO_CHAR(order_date, 'DD-Mon-YYYY') AS formatted1,       -- 15-Jan-2024
       TO_CHAR(order_date, 'YYYY/MM/DD') AS formatted2,        -- 2024/01/15
       TO_CHAR(order_date, 'Day, DD Month YYYY') AS formatted3, -- Monday, 15 January 2024
       TO_CHAR(order_date, 'HH12:MI AM') AS time_formatted     -- 10:30 AM
FROM orders LIMIT 3;

-- ============================================
-- TO_DATE() / TO_TIMESTAMP() — parse strings
-- ============================================

SELECT TO_DATE('15-01-2024', 'DD-MM-YYYY') AS parsed_date;
SELECT TO_TIMESTAMP('2024-01-15 10:30:00', 'YYYY-MM-DD HH24:MI:SS') AS parsed_ts;

-- ============================================
-- INTERVAL arithmetic
-- ============================================

-- add 30 days to order date
SELECT order_date,
       order_date + INTERVAL '30 days' AS plus_30_days
FROM orders LIMIT 3;

-- subtract 1 month
SELECT CURRENT_DATE - INTERVAL '1 month' AS one_month_ago;

-- add 2 hours 30 minutes
SELECT NOW() + INTERVAL '2 hours 30 minutes' AS later;

-- orders from last 6 months
SELECT * FROM orders
WHERE order_date >= NOW() - INTERVAL '6 months';

-- various intervals
SELECT CURRENT_DATE + INTERVAL '1 year' AS next_year;
SELECT CURRENT_DATE + INTERVAL '1 year 3 months 10 days' AS complex_interval;

-- ============================================
-- AT TIME ZONE
-- ============================================

-- convert to IST (Indian Standard Time)
SELECT order_date,
       order_date AT TIME ZONE 'Asia/Kolkata' AS ist_time,
       order_date AT TIME ZONE 'US/Eastern' AS est_time
FROM orders LIMIT 3;

-- show current time in different zones
SELECT
    NOW() AT TIME ZONE 'UTC' AS utc,
    NOW() AT TIME ZONE 'Asia/Kolkata' AS ist,
    NOW() AT TIME ZONE 'US/Pacific' AS pst;

-- ============================================
-- generate_series() for date ranges
-- ============================================

-- generate all dates in January 2024
SELECT d::date AS date
FROM generate_series('2024-01-01', '2024-01-31', INTERVAL '1 day') AS d;

-- generate months for a year
SELECT d::date AS month_start
FROM generate_series('2024-01-01', '2024-12-01', INTERVAL '1 month') AS d;

-- find days with no orders (using LEFT JOIN)
SELECT d::date AS date, o.id
FROM generate_series('2024-01-01', '2024-12-31', INTERVAL '1 day') AS d
LEFT JOIN orders o ON d::date = o.order_date::date
WHERE o.id IS NULL
LIMIT 20;

-- ============================================
-- PRACTICAL: Reporting queries
-- ============================================

-- monthly revenue report with month name
SELECT
    TO_CHAR(DATE_TRUNC('month', order_date), 'Month YYYY') AS month,
    COUNT(*) AS total_orders,
    SUM(amount) AS revenue,
    ROUND(AVG(amount), 2) AS avg_order_value
FROM orders
GROUP BY DATE_TRUNC('month', order_date)
ORDER BY DATE_TRUNC('month', order_date);

-- orders by day of week
SELECT
    TO_CHAR(order_date, 'Day') AS day_name,
    EXTRACT(DOW FROM order_date) AS day_num,
    COUNT(*) AS order_count
FROM orders
GROUP BY TO_CHAR(order_date, 'Day'), EXTRACT(DOW FROM order_date)
ORDER BY day_num;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Find all orders placed in Q1 (Jan-Mar) 2024

-- Q2: Calculate average delivery time (delivery_date - order_date) per customer

-- Q3: Show orders with the day name and whether it's a weekday or weekend

-- Q4: Generate a report showing every month of 2024 with order count
--     (including months with 0 orders)

-- Q5: Find the customer who placed the most recent order

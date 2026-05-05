-- ============================================
-- 09 - Window Functions Practice (Postgres)
-- ============================================

DROP TABLE IF EXISTS sales;
CREATE TABLE sales (
    id SERIAL PRIMARY KEY,
    salesperson TEXT,
    region TEXT,
    sale_date DATE,
    amount NUMERIC(10,2)
);

INSERT INTO sales (salesperson, region, sale_date, amount) VALUES
('Alice', 'North', '2024-01-05', 500),
('Alice', 'North', '2024-01-15', 700),
('Alice', 'North', '2024-02-10', 600),
('Alice', 'North', '2024-03-20', 900),
('Bob', 'South', '2024-01-10', 450),
('Bob', 'South', '2024-02-05', 550),
('Bob', 'South', '2024-02-25', 800),
('Bob', 'South', '2024-03-15', 650),
('Charlie', 'North', '2024-01-20', 300),
('Charlie', 'North', '2024-02-15', 400),
('Charlie', 'North', '2024-03-10', 500),
('Diana', 'South', '2024-01-08', 800),
('Diana', 'South', '2024-02-18', 750),
('Diana', 'South', '2024-03-28', 900);

-- ============================================
-- OVER() — basic window (entire result set)
-- ============================================

-- compare each sale to the overall total
SELECT salesperson, amount,
       SUM(amount) OVER() AS grand_total,
       ROUND(amount / SUM(amount) OVER() * 100, 2) AS pct_of_total
FROM sales;

-- ============================================
-- PARTITION BY — group the window
-- ============================================

-- compare each sale to their salesperson's total
SELECT salesperson, sale_date, amount,
       SUM(amount) OVER(PARTITION BY salesperson) AS person_total,
       ROUND(amount / SUM(amount) OVER(PARTITION BY salesperson) * 100, 1) AS pct
FROM sales
ORDER BY salesperson, sale_date;

-- ============================================
-- ROW_NUMBER() — unique sequential number
-- ============================================

-- number each sale per salesperson by date
SELECT salesperson, sale_date, amount,
       ROW_NUMBER() OVER(PARTITION BY salesperson ORDER BY sale_date) AS sale_num
FROM sales;

-- get the FIRST sale of each salesperson (top-1 per group)
SELECT * FROM (
    SELECT *, ROW_NUMBER() OVER(PARTITION BY salesperson ORDER BY sale_date) AS rn
    FROM sales
) sub WHERE rn = 1;

-- ============================================
-- RANK() vs DENSE_RANK() vs ROW_NUMBER()
-- ============================================

-- RANK: gaps on ties, DENSE_RANK: no gaps, ROW_NUMBER: always unique
SELECT salesperson, amount,
       ROW_NUMBER() OVER(ORDER BY amount DESC) AS row_num,
       RANK()       OVER(ORDER BY amount DESC) AS rank,
       DENSE_RANK() OVER(ORDER BY amount DESC) AS dense_rank
FROM sales;

-- top 3 sales per region
SELECT * FROM (
    SELECT region, salesperson, amount,
           DENSE_RANK() OVER(PARTITION BY region ORDER BY amount DESC) AS rnk
    FROM sales
) ranked WHERE rnk <= 3;

-- ============================================
-- NTILE(n) — divide into n equal buckets
-- ============================================

-- divide all sales into 4 quartiles
SELECT salesperson, amount,
       NTILE(4) OVER(ORDER BY amount) AS quartile
FROM sales;

-- label as Low/Medium/High/Premium
SELECT salesperson, amount,
       CASE NTILE(4) OVER(ORDER BY amount)
           WHEN 1 THEN 'Low'
           WHEN 2 THEN 'Medium'
           WHEN 3 THEN 'High'
           WHEN 4 THEN 'Premium'
       END AS tier
FROM sales;

-- ============================================
-- LAG() / LEAD() — access previous/next rows
-- ============================================

-- compare each sale to the PREVIOUS sale (per person)
SELECT salesperson, sale_date, amount,
       LAG(amount) OVER(PARTITION BY salesperson ORDER BY sale_date) AS prev_amount,
       amount - LAG(amount) OVER(PARTITION BY salesperson ORDER BY sale_date) AS change
FROM sales;

-- compare to NEXT sale
SELECT salesperson, sale_date, amount,
       LEAD(amount) OVER(PARTITION BY salesperson ORDER BY sale_date) AS next_amount
FROM sales;

-- LAG with offset of 2 and default value
SELECT salesperson, sale_date, amount,
       LAG(amount, 2, 0) OVER(PARTITION BY salesperson ORDER BY sale_date) AS two_back
FROM sales;

-- ============================================
-- FIRST_VALUE / LAST_VALUE / NTH_VALUE
-- ============================================

-- compare each sale to the first and best sale per person
SELECT salesperson, sale_date, amount,
       FIRST_VALUE(amount) OVER(PARTITION BY salesperson ORDER BY sale_date) AS first_sale,
       LAST_VALUE(amount) OVER(
           PARTITION BY salesperson ORDER BY sale_date
           ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
       ) AS last_sale,
       NTH_VALUE(amount, 2) OVER(PARTITION BY salesperson ORDER BY sale_date) AS second_sale
FROM sales;

-- ============================================
-- Running Totals / Moving Averages
-- ============================================

-- running total per salesperson
SELECT salesperson, sale_date, amount,
       SUM(amount) OVER(
           PARTITION BY salesperson
           ORDER BY sale_date
           ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
       ) AS running_total
FROM sales;

-- running count
SELECT salesperson, sale_date,
       COUNT(*) OVER(
           PARTITION BY salesperson
           ORDER BY sale_date
       ) AS running_count
FROM sales;

-- 3-row moving average
SELECT salesperson, sale_date, amount,
       ROUND(AVG(amount) OVER(
           PARTITION BY salesperson
           ORDER BY sale_date
           ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING
       ), 2) AS moving_avg_3
FROM sales;

-- ============================================
-- Frame Clause: ROWS BETWEEN
-- ============================================

-- sliding window: sum of current + previous 2 rows
SELECT salesperson, sale_date, amount,
       SUM(amount) OVER(
           PARTITION BY salesperson
           ORDER BY sale_date
           ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
       ) AS sum_last_3
FROM sales;

-- entire partition (explicit frame)
SELECT salesperson, amount,
       SUM(amount) OVER(
           PARTITION BY salesperson
           ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
       ) AS total
FROM sales;

-- ============================================
-- PERCENT_RANK / CUME_DIST
-- ============================================

-- PERCENT_RANK: relative rank as percentage (0 to 1)
-- CUME_DIST: cumulative distribution (fraction of rows <= current)
SELECT salesperson, amount,
       ROUND(PERCENT_RANK() OVER(ORDER BY amount)::NUMERIC, 3) AS pct_rank,
       ROUND(CUME_DIST() OVER(ORDER BY amount)::NUMERIC, 3) AS cum_dist
FROM sales;

-- ============================================
-- Named Windows (WINDOW clause) — avoid repetition
-- ============================================

SELECT salesperson, sale_date, amount,
       SUM(amount)   OVER w AS running_total,
       AVG(amount)   OVER w AS running_avg,
       COUNT(*)      OVER w AS running_count,
       ROW_NUMBER()  OVER w AS row_num
FROM sales
WINDOW w AS (PARTITION BY salesperson ORDER BY sale_date)
ORDER BY salesperson, sale_date;

-- ============================================
-- PRACTICAL: Sales dashboard query
-- ============================================

SELECT
    salesperson,
    region,
    sale_date,
    amount,
    -- running total for the person
    SUM(amount) OVER(PARTITION BY salesperson ORDER BY sale_date) AS person_running,
    -- rank within region
    RANK() OVER(PARTITION BY region ORDER BY amount DESC) AS region_rank,
    -- change from previous sale
    amount - LAG(amount) OVER(PARTITION BY salesperson ORDER BY sale_date) AS delta,
    -- percentage of region total
    ROUND(amount / SUM(amount) OVER(PARTITION BY region) * 100, 1) AS pct_region
FROM sales
ORDER BY region, salesperson, sale_date;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: For each salesperson, show their running total and
--     what % of their final total each sale represents

-- Q2: Find the sale with the biggest increase from the previous sale
--     (per salesperson)

-- Q3: Rank all salespersons by their total sales. Show their rank
--     and the gap to the person above them

-- Q4: Using NTILE, divide salespersons into 2 tiers (top/bottom)
--     based on total sales

-- Q5: Calculate a 2-sale moving average for each salesperson
--     and flag any sale where amount is 50%+ above the moving avg

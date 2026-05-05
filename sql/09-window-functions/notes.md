# 09 - Window Functions

> The #1 topic that separates junior from senior SQL developers.

---

## What is a Window Function?

Performs calculation across rows **without collapsing them** (unlike GROUP BY).

```sql
-- GROUP BY: collapses rows (1 row per department)
SELECT department, AVG(salary) FROM employees GROUP BY department;

-- Window function: keeps ALL rows, adds the aggregate next to each
SELECT name, department, salary,
       AVG(salary) OVER(PARTITION BY department) AS dept_avg
FROM employees;
```

```
 name    | department  | salary | dept_avg
---------+-------------+--------+---------
 Alice   | Engineering | 95000  | 90000
 Bob     | Engineering | 85000  | 90000
 Charlie | Sales       | 65000  | 68500
 Diana   | Sales       | 72000  | 68500
```

> Every row is preserved. The window function adds a computed column.

---

## Anatomy of OVER()

```sql
function_name() OVER (
    PARTITION BY column      -- divide into groups (optional)
    ORDER BY column          -- order within each group (optional)
    ROWS BETWEEN ... AND ... -- frame: which rows to include (optional)
)
```

---

## ROW_NUMBER() — Unique Sequential Number

```sql
SELECT name, department, salary,
       ROW_NUMBER() OVER(PARTITION BY department ORDER BY salary DESC) AS rank
FROM employees;
```

```
 name    | department  | salary | rank
---------+-------------+--------+-----
 Alice   | Engineering | 95000  | 1
 Bob     | Engineering | 85000  | 2
 Diana   | Sales       | 72000  | 1
 Charlie | Sales       | 65000  | 2
```

**Top-N per group pattern:**
```sql
SELECT * FROM (
    SELECT *, ROW_NUMBER() OVER(PARTITION BY department ORDER BY salary DESC) AS rn
    FROM employees
) sub WHERE rn <= 3;   -- top 3 per department
```

---

## RANK() vs DENSE_RANK() vs ROW_NUMBER()

```sql
-- data: salaries = 100, 90, 90, 80
SELECT salary,
    ROW_NUMBER() OVER(ORDER BY salary DESC) AS row_num,    -- 1, 2, 3, 4
    RANK()       OVER(ORDER BY salary DESC) AS rank,       -- 1, 2, 2, 4  (gap!)
    DENSE_RANK() OVER(ORDER BY salary DESC) AS dense_rank  -- 1, 2, 2, 3  (no gap)
FROM employees;
```

| Function | Ties | Gaps |
|----------|------|------|
| ROW_NUMBER | breaks ties arbitrarily | never |
| RANK | same rank for ties | yes (skips numbers) |
| DENSE_RANK | same rank for ties | no |

---

## LAG() / LEAD() — Previous / Next Row

```sql
-- compare each sale to the PREVIOUS one
SELECT salesperson, sale_date, amount,
    LAG(amount) OVER(PARTITION BY salesperson ORDER BY sale_date) AS prev_amount,
    amount - LAG(amount) OVER(PARTITION BY salesperson ORDER BY sale_date) AS change
FROM sales;
```

```
 salesperson | sale_date  | amount | prev_amount | change
-------------+------------+--------+-------------+-------
 Alice       | 2024-01-05 | 500    | NULL        | NULL
 Alice       | 2024-01-15 | 700    | 500         | 200
 Alice       | 2024-02-10 | 600    | 700         | -100
```

```sql
-- LAG with offset and default
LAG(amount, 2, 0)  -- 2 rows back, default 0 if no row

-- LEAD = look forward
LEAD(amount) OVER(... ORDER BY sale_date)  -- next row's value
```

**Month-over-month growth:**
```sql
SELECT month, revenue,
    ROUND((revenue - LAG(revenue) OVER(ORDER BY month))
        / LAG(revenue) OVER(ORDER BY month) * 100, 1) AS growth_pct
FROM monthly_revenue;
```

---

## NTILE(n) — Divide Into n Buckets

```sql
SELECT name, salary,
    NTILE(4) OVER(ORDER BY salary) AS quartile
FROM employees;
```

```
 name    | salary | quartile
---------+--------+---------
 Charlie | 65000  | 1        -- bottom 25%
 Diana   | 72000  | 2
 Bob     | 85000  | 3
 Alice   | 95000  | 4        -- top 25%
```

---

## FIRST_VALUE / LAST_VALUE / NTH_VALUE

```sql
SELECT salesperson, sale_date, amount,
    FIRST_VALUE(amount) OVER w AS first_sale,
    LAST_VALUE(amount)  OVER w AS last_sale,
    NTH_VALUE(amount,2) OVER w AS second_sale
FROM sales
WINDOW w AS (
    PARTITION BY salesperson ORDER BY sale_date
    ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
);
```

> LAST_VALUE needs explicit frame (`ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING`). Without it, it only sees up to current row.

---

## Running Total / Running Average

```sql
-- running total
SELECT sale_date, amount,
    SUM(amount) OVER(ORDER BY sale_date) AS running_total
FROM sales;
```

```
 sale_date  | amount | running_total
------------+--------+--------------
 2024-01-05 | 500    | 500
 2024-01-10 | 450    | 950
 2024-01-15 | 700    | 1650
 2024-01-20 | 300    | 1950
```

```sql
-- running average
SELECT sale_date, amount,
    ROUND(AVG(amount) OVER(ORDER BY sale_date), 2) AS running_avg
FROM sales;

-- per salesperson
SELECT salesperson, sale_date, amount,
    SUM(amount) OVER(PARTITION BY salesperson ORDER BY sale_date) AS person_running
FROM sales;
```

---

## Frame Clause — ROWS BETWEEN

Controls exactly which rows the window function sees.

```sql
-- default when ORDER BY is present:
ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW    -- running total

-- sliding window: 3-row moving average
ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING

-- entire partition
ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING

-- from current to end
ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING
```

```sql
-- 3-row moving average
SELECT sale_date, amount,
    ROUND(AVG(amount) OVER(
        ORDER BY sale_date
        ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING
    ), 2) AS moving_avg_3
FROM sales;
```

---

## PERCENT_RANK / CUME_DIST

```sql
SELECT name, salary,
    ROUND(PERCENT_RANK() OVER(ORDER BY salary)::NUMERIC, 2) AS pct_rank,
    ROUND(CUME_DIST()    OVER(ORDER BY salary)::NUMERIC, 2) AS cum_dist
FROM employees;
```

| Function | Returns | Range |
|----------|---------|-------|
| PERCENT_RANK | relative position | 0.0 to 1.0 |
| CUME_DIST | fraction of rows <= current | 0.0 to 1.0 |

---

## Named Window — Avoid Repetition

```sql
SELECT salesperson, sale_date, amount,
    SUM(amount)  OVER w AS running_total,
    AVG(amount)  OVER w AS running_avg,
    ROW_NUMBER() OVER w AS row_num
FROM sales
WINDOW w AS (PARTITION BY salesperson ORDER BY sale_date);
```

> Define `WINDOW w AS (...)` once, use it multiple times. Cleaner SQL.

---

## Execution Order

```
1. FROM / JOIN
2. WHERE         -- window functions NOT available here
3. GROUP BY
4. HAVING
5. SELECT        -- window functions computed here
6. ORDER BY
7. LIMIT
```

> You CANNOT use window functions in WHERE. Wrap in a CTE or subquery:
```sql
WITH ranked AS (
    SELECT *, RANK() OVER(ORDER BY salary DESC) AS rnk FROM employees
)
SELECT * FROM ranked WHERE rnk <= 5;
```

---

## All Window Functions at a Glance

| Function | Purpose | Example Use |
|----------|---------|-------------|
| `ROW_NUMBER()` | unique row number | top-N per group, dedup |
| `RANK()` | rank with gaps | leaderboards |
| `DENSE_RANK()` | rank without gaps | leaderboards |
| `NTILE(n)` | divide into buckets | quartile analysis |
| `LAG(col, n)` | previous row value | MoM growth, change detection |
| `LEAD(col, n)` | next row value | forecast comparison |
| `FIRST_VALUE()` | first in window | compare to first record |
| `LAST_VALUE()` | last in window | compare to last record |
| `NTH_VALUE(col,n)` | nth value | specific position |
| `SUM() OVER` | running sum | cumulative totals |
| `AVG() OVER` | running average | moving averages |
| `COUNT() OVER` | running count | sequence numbering |
| `PERCENT_RANK()` | percentile rank | statistical analysis |
| `CUME_DIST()` | cumulative distribution | statistical analysis |

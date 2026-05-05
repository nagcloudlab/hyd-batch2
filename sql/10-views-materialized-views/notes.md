# 10 - Views & Materialized Views

---

## CREATE VIEW — A Saved Query

A view is NOT a table. It stores the query, not the data. Every time you SELECT from it, the query runs.

```sql
CREATE VIEW v_active_employees AS
SELECT id, name, department, salary
FROM employees
WHERE is_active = true;

-- use it like a table
SELECT * FROM v_active_employees;
SELECT * FROM v_active_employees WHERE department = 'Engineering';
```

---

## CREATE OR REPLACE VIEW

Modify an existing view without dropping it. Can add columns at the end.

```sql
CREATE OR REPLACE VIEW v_active_employees AS
SELECT id, name, department, salary,
       CASE WHEN salary > 90000 THEN 'Senior' ELSE 'Junior' END AS level
FROM employees
WHERE is_active = true;
```

---

## View for Security — Hide Sensitive Data

```sql
-- expose to analysts (no salary visible)
CREATE VIEW v_employee_directory AS
SELECT id, name, department FROM employees;

-- GRANT SELECT ON v_employee_directory TO analyst_role;
-- now analysts can't see salary at all
```

---

## View with Joins — Simplify Complex Queries

```sql
CREATE VIEW v_sales_dashboard AS
SELECT
    e.name AS salesperson,
    COUNT(s.id) AS total_sales,
    SUM(s.amount) AS revenue,
    ROUND(AVG(s.amount), 2) AS avg_sale
FROM employees e
LEFT JOIN sales s ON e.id = s.emp_id
WHERE e.department = 'Sales'
GROUP BY e.name;

-- analysts just do:
SELECT * FROM v_sales_dashboard;
```

---

## DROP VIEW

```sql
DROP VIEW v_sales_dashboard;
DROP VIEW IF EXISTS v_sales_dashboard;   -- no error if missing
```

---

## MATERIALIZED VIEW — Cached Query Result

Stores the actual data. Fast reads. Must be manually refreshed.

```sql
CREATE MATERIALIZED VIEW mv_monthly_revenue AS
SELECT
    DATE_TRUNC('month', order_date)::DATE AS month,
    SUM(amount) AS revenue,
    COUNT(*) AS order_count
FROM orders
GROUP BY DATE_TRUNC('month', order_date)
ORDER BY month;

-- query it (reads stored data — instant)
SELECT * FROM mv_monthly_revenue;
```

---

## REFRESH MATERIALIZED VIEW

Data is stale until you refresh it.

```sql
-- basic refresh (blocks reads during refresh)
REFRESH MATERIALIZED VIEW mv_monthly_revenue;

-- concurrent refresh (no read lock — needs UNIQUE INDEX)
CREATE UNIQUE INDEX idx_mv_month ON mv_monthly_revenue(month);
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_monthly_revenue;
```

> In production, schedule refresh with cron or pg_cron extension.

---

## Index on Materialized View

Materialized views CAN have indexes (regular views CANNOT).

```sql
CREATE INDEX idx_mv_revenue ON mv_monthly_revenue(revenue);

-- now fast:
SELECT * FROM mv_monthly_revenue WHERE revenue > 100000;
```

---

## List Views & Materialized Views

```sql
-- list views
SELECT table_name FROM information_schema.views WHERE table_schema = 'public';

-- list materialized views
SELECT matviewname FROM pg_matviews WHERE schemaname = 'public';
```

---

## View vs Materialized View

| Feature | View | Materialized View |
|---------|------|-------------------|
| Stores data? | No (runs query each time) | Yes (cached on disk) |
| Read speed | same as base query | fast (pre-computed) |
| Data freshness | always current | stale until REFRESH |
| Can have indexes? | No | Yes |
| Disk space | none | uses storage |
| Best for | simplifying queries, security | dashboards, slow reports |

---

## When to Use What

| Scenario | Use |
|----------|-----|
| Hide columns from users | View |
| Simplify a complex JOIN for analysts | View |
| Dashboard that runs a 10-second aggregation | Materialized View |
| Data that changes rarely (daily reports) | Materialized View |
| Real-time data needed | View |
| Need to index the result | Materialized View |

# 05 - Advanced Joins

---

## Quick Recap: INNER / LEFT / RIGHT

```sql
-- INNER: only matching rows
SELECT * FROM orders o INNER JOIN customers c ON o.customer_id = c.id;

-- LEFT: all from left + matches from right (NULLs if no match)
SELECT * FROM customers c LEFT JOIN orders o ON c.id = o.customer_id;

-- RIGHT: all from right + matches from left
SELECT * FROM customers c RIGHT JOIN orders o ON c.id = o.customer_id;
```

---

## FULL OUTER JOIN — All Rows From Both

Returns all rows from both tables. NULLs where there's no match.

```sql
SELECT c.name, o.id AS order_id, o.total
FROM customers c
FULL OUTER JOIN orders o ON c.id = o.customer_id;
```

```
 name    | order_id | total
---------+----------+-------
 Alice   | 1        | 500     -- matched
 Alice   | 2        | 800     -- matched
 Bob     | 3        | 1200    -- matched
 Charlie | NULL     | NULL    -- customer with no order
 NULL    | 5        | 150     -- order with no customer
```

**Real use:** find mismatches between two systems.

```sql
-- find unmatched rows on either side
SELECT * FROM table_a a
FULL OUTER JOIN table_b b ON a.id = b.id
WHERE a.id IS NULL OR b.id IS NULL;
```

---

## CROSS JOIN — Cartesian Product (Every Combination)

No ON clause. M rows x N rows = M*N result rows.

```sql
-- every student x every course
SELECT s.name, c.course_name
FROM students s
CROSS JOIN courses c;
```

```
-- 3 students x 4 courses = 12 rows
 Alice  | SQL
 Alice  | Python
 Alice  | Java
 Alice  | React
 Bob    | SQL
 Bob    | Python
 ...
```

**Real use:** generate all combinations for reports.

```sql
-- every month x every category (for a report with zero-fill)
SELECT m.month, c.category
FROM generate_series(1,12) AS m(month)
CROSS JOIN (SELECT DISTINCT category FROM products) AS c;
```

> WARNING: CROSS JOIN on large tables = explosion. 10K x 10K = 100M rows!

---

## SELF JOIN — Table Joined to Itself

Requires aliases to distinguish the two copies of the same table.

```sql
-- employees table has: id, name, manager_id
-- show each employee with their manager's name
SELECT
    e.name  AS employee,
    m.name  AS manager
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.id;
```

```
 employee  | manager
-----------+---------
 CEO       | NULL       -- no manager
 CTO       | CEO
 Dev Lead  | CTO
 Dev1      | Dev Lead
```

**Find pairs** (colleagues with same manager):

```sql
SELECT e1.name, e2.name, m.name AS shared_manager
FROM employees e1
JOIN employees e2 ON e1.manager_id = e2.manager_id AND e1.id < e2.id
JOIN employees m ON e1.manager_id = m.id;
```

> `e1.id < e2.id` prevents duplicate pairs (Alice-Bob and Bob-Alice).

---

## USING Clause — Shorthand When Column Names Match

```sql
-- instead of: ON emp.dept_id = dept.dept_id
SELECT e.name, d.dept_name
FROM emp e
JOIN dept d USING (dept_id);
```

> Only works when the join column has the SAME NAME in both tables.

---

## NATURAL JOIN — Auto-Match Same Column Names

```sql
SELECT * FROM emp NATURAL JOIN dept;
-- auto-joins on ALL columns with matching names (dept_id here)
```

> AVOID in production. If you add a column `name` to both tables later, the join silently changes behavior.

---

## Multiple JOIN Conditions

```sql
-- join on two conditions
SELECT p.name, pr.region, pr.price
FROM products p
JOIN prices pr ON p.id = pr.product_id AND pr.region = 'South';
```

---

## Non-Equi Join — Join on Inequality

```sql
-- salary band lookup (range join)
CREATE TABLE salary_bands (band TEXT, min_sal NUMERIC, max_sal NUMERIC);
-- Junior: 0-50000, Mid: 50001-90000, Senior: 90001-150000

SELECT e.name, e.salary, sb.band
FROM employees e
JOIN salary_bands sb ON e.salary BETWEEN sb.min_sal AND sb.max_sal;
```

```
 name    | salary | band
---------+--------+--------
 Alice   | 40000  | Junior
 Bob     | 75000  | Mid
 Charlie | 120000 | Senior
```

---

## LEFT JOIN Gotcha: WHERE vs ON

```sql
-- WRONG: this turns LEFT JOIN into INNER JOIN!
SELECT * FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id
WHERE o.status = 'active';    -- filters out NULLs (no-match rows disappear)

-- CORRECT: put the filter in ON
SELECT * FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id AND o.status = 'active';
```

---

## Join Comparison Table

| Join | Result | Use Case |
|------|--------|----------|
| `INNER JOIN` | only matches | most common |
| `LEFT JOIN` | all left + matches right | "show all X, with Y if exists" |
| `RIGHT JOIN` | all right + matches left | rarely used (reverse LEFT) |
| `FULL OUTER JOIN` | all from both | find mismatches |
| `CROSS JOIN` | every combination | report templates, grids |
| `SELF JOIN` | table to itself | hierarchies, pairs |

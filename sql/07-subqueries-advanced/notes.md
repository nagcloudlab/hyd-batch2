# 07 - Advanced Subqueries

---

## Subquery in WHERE — IN / NOT IN

```sql
-- customers who placed at least one order
SELECT * FROM customers
WHERE id IN (SELECT customer_id FROM orders);

-- customers who NEVER ordered
SELECT * FROM customers
WHERE id NOT IN (SELECT customer_id FROM orders WHERE customer_id IS NOT NULL);
```

> WARNING: `NOT IN` breaks when subquery returns NULL. Always filter NULLs or use `NOT EXISTS`.

---

## Subquery in SELECT — Scalar Subquery

Returns exactly ONE value. Runs once per row of outer query.

```sql
-- each customer with their order count
SELECT
    name,
    (SELECT COUNT(*) FROM orders o WHERE o.customer_id = c.id) AS order_count
FROM customers c;
```

```
 name    | order_count
---------+------------
 Alice   | 3
 Bob     | 2
 Charlie | 0
```

```sql
-- each product vs its category average
SELECT name, price,
    price - (SELECT AVG(price) FROM products p2 WHERE p2.category = p.category) AS vs_avg
FROM products p;
```

---

## Subquery in FROM — Derived Table

The subquery acts like a temporary table. Must have an alias.

```sql
-- find customers who spent more than 1000
SELECT summary.name, summary.total_spent
FROM (
    SELECT c.name, COALESCE(SUM(o.total), 0) AS total_spent
    FROM customers c
    LEFT JOIN orders o ON c.id = o.customer_id
    GROUP BY c.name
) AS summary
WHERE summary.total_spent > 1000;
```

---

## EXISTS / NOT EXISTS

Checks if the subquery returns ANY rows. Stops at first match (fast).

```sql
-- customers who have at least one order
SELECT c.name FROM customers c
WHERE EXISTS (
    SELECT 1 FROM orders o WHERE o.customer_id = c.id
);

-- customers with NO orders
SELECT c.name FROM customers c
WHERE NOT EXISTS (
    SELECT 1 FROM orders o WHERE o.customer_id = c.id
);
```

> EXISTS is usually faster than IN for large datasets. It short-circuits at first match.

**EXISTS vs IN:**
```
IN:     collects ALL results from subquery, then checks membership
EXISTS: stops at FIRST matching row (more efficient for large subqueries)
```

---

## ANY / ALL Operators

```sql
-- price greater than ANY clothing item (= at least one)
SELECT * FROM products WHERE price > ANY (
    SELECT price FROM products WHERE category = 'Clothing'
);
-- same as: price > MIN(clothing prices)

-- price greater than ALL clothing items (= every one)
SELECT * FROM products WHERE price > ALL (
    SELECT price FROM products WHERE category = 'Clothing'
);
-- same as: price > MAX(clothing prices)
```

| Operator | Meaning |
|----------|---------|
| `> ANY(...)` | greater than at least one value |
| `> ALL(...)` | greater than every value |
| `= ANY(...)` | same as `IN (...)` |
| `<> ALL(...)` | same as `NOT IN (...)` |

---

## Correlated Subquery

Inner query references the outer query. Runs once per outer row.

```sql
-- orders above the customer's own average
SELECT o.id, c.name, o.total
FROM orders o
JOIN customers c ON o.customer_id = c.id
WHERE o.total > (
    SELECT AVG(total) FROM orders o2 WHERE o2.customer_id = o.customer_id
);
```

```sql
-- each customer's most recent order date
SELECT c.name,
    (SELECT MAX(order_date) FROM orders o WHERE o.customer_id = c.id) AS last_order
FROM customers c;
```

> Correlated subqueries can be slow (N outer rows = N subquery executions). Often rewritable as JOIN or window function.

---

## LATERAL Join (Postgres Specific)

Like a correlated subquery in FROM — the subquery can reference the outer table.

```sql
-- for each customer, get their latest 2 orders
SELECT c.name, latest.order_date, latest.total
FROM customers c
LEFT JOIN LATERAL (
    SELECT order_date, total
    FROM orders o
    WHERE o.customer_id = c.id
    ORDER BY order_date DESC
    LIMIT 2
) latest ON true
ORDER BY c.name, latest.order_date DESC;
```

```
 name    | order_date | total
---------+------------+------
 Alice   | 2024-05-20 | 1200
 Alice   | 2024-02-15 | 800
 Bob     | 2024-06-01 | 300
 Bob     | 2024-01-20 | 1200
 Charlie | NULL       | NULL    -- no orders
```

> LATERAL is Postgres's "for each row, run this subquery" — extremely powerful for top-N-per-group queries.

---

## When to Use What

| Need | Use |
|------|-----|
| filter by related data exists | `EXISTS` |
| filter by list of values | `IN` |
| filter by negative (no related data) | `NOT EXISTS` (safer than NOT IN) |
| add a computed column per row | scalar subquery in SELECT |
| use query result as a table | subquery in FROM (derived table) |
| top-N per group | `LATERAL` join |
| compare to aggregate of same table | correlated subquery or window function |

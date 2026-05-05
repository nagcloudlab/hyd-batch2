# 01 - Intermediate Querying

---

## DISTINCT — Remove Duplicates

```sql
-- unique departments
SELECT DISTINCT department FROM employees;

-- unique combinations
SELECT DISTINCT city, department FROM employees;

-- count unique values
SELECT COUNT(DISTINCT city) AS unique_cities FROM employees;
```

---

## LIKE / ILIKE — Pattern Matching

| Pattern | Meaning |
|---------|---------|
| `%` | any number of characters |
| `_` | exactly one character |

```sql
-- starts with 'A'
SELECT * FROM employees WHERE name LIKE 'A%';

-- contains 'kumar' (case-insensitive, Postgres only)
SELECT * FROM employees WHERE name ILIKE '%kumar%';

-- 3rd character is 'e'
SELECT * FROM employees WHERE name LIKE '__e%';

-- NOT LIKE
SELECT * FROM employees WHERE email NOT LIKE '%gmail%';
```

> ILIKE is Postgres-specific. MySQL uses `LIKE` with `COLLATE` for case-insensitivity.

---

## BETWEEN — Range Filter (Inclusive)

```sql
-- salary between 50k and 90k
SELECT * FROM employees WHERE salary BETWEEN 50000 AND 90000;

-- dates in 2024
SELECT * FROM orders WHERE order_date BETWEEN '2024-01-01' AND '2024-12-31';

-- NOT BETWEEN
SELECT * FROM employees WHERE salary NOT BETWEEN 50000 AND 90000;
```

> BETWEEN includes both endpoints: `BETWEEN 10 AND 20` matches 10 and 20.

---

## IN — Match Against a List

```sql
-- specific cities
SELECT * FROM employees WHERE city IN ('Hyderabad', 'Mumbai', 'Chennai');

-- NOT IN
SELECT * FROM employees WHERE department NOT IN ('HR', 'Marketing');
```

> WARNING: `NOT IN` with NULLs returns zero rows. Prefer `NOT EXISTS` for safety.

---

## LIMIT / OFFSET — Pagination

```sql
-- first 10 rows
SELECT * FROM employees ORDER BY id LIMIT 10;

-- page 2 (skip 10, get next 10)
SELECT * FROM employees ORDER BY id LIMIT 10 OFFSET 10;

-- top 5 highest salaries
SELECT name, salary FROM employees ORDER BY salary DESC LIMIT 5;
```

> OFFSET is slow on large tables (scans skipped rows). For production, use keyset pagination:
> `WHERE id > last_seen_id ORDER BY id LIMIT 10`

---

## CASE WHEN — Conditional Logic

```sql
-- categorize in SELECT
SELECT name, salary,
    CASE
        WHEN salary >= 100000 THEN 'High'
        WHEN salary >= 70000  THEN 'Medium'
        ELSE 'Low'
    END AS salary_band
FROM employees;

-- inside aggregate (pivot-style)
SELECT department,
    COUNT(CASE WHEN salary >= 80000 THEN 1 END) AS high_earners,
    COUNT(CASE WHEN salary <  80000 THEN 1 END) AS low_earners
FROM employees GROUP BY department;

-- custom sort order
SELECT * FROM employees
ORDER BY CASE department
    WHEN 'Engineering' THEN 1
    WHEN 'HR' THEN 2
    ELSE 3
END;
```

---

## COALESCE — First Non-NULL Value

```sql
-- show nickname, fallback to name, fallback to 'Unknown'
SELECT COALESCE(nickname, name, 'Unknown') AS display_name FROM users;

-- safe math with NULLs
SELECT COALESCE(bonus, 0) + salary AS total_pay FROM employees;
```

> COALESCE takes any number of arguments. Returns the first one that is NOT NULL.

---

## NULLIF — Return NULL If Equal

```sql
-- avoid division by zero
SELECT revenue / NULLIF(cost, 0) AS margin FROM products;

-- treat empty string as NULL
SELECT NULLIF(phone, '') AS phone FROM contacts;
```

> `NULLIF(a, b)` = returns NULL if `a = b`, otherwise returns `a`.

---

## IS NULL / IS NOT NULL

```sql
-- find rows with missing data
SELECT * FROM employees WHERE phone IS NULL;

-- find rows with data present
SELECT * FROM employees WHERE phone IS NOT NULL;
```

> Never use `= NULL`. It always returns false. Always use `IS NULL`.

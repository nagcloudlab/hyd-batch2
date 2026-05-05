# 14a - User Defined Functions (UDFs) in Postgres

> In Postgres, every function you create is a UDF. There is no separate "UDF" keyword — you just use `CREATE FUNCTION`.

---

## What is a UDF?

A reusable piece of logic stored in the database. Called like a built-in function.

```sql
SELECT my_function(arg1, arg2);          -- scalar UDF
SELECT * FROM my_table_function(arg1);   -- table-returning UDF
```

---

## Types of UDFs in Postgres

| Type | RETURNS | Example Use |
|------|---------|-------------|
| Scalar | single value (INT, TEXT, NUMERIC...) | calc_tax(1000) -> 180 |
| Table | TABLE(...) | get_top_earners(5) -> rows |
| Set-returning | SETOF type | get_all_active() -> multiple rows |
| Void | VOID | log_event('click') -> no return |
| Trigger | TRIGGER | auto-set updated_at |

---

## UDF Languages in Postgres

```sql
$$ LANGUAGE SQL;         -- pure SQL (simplest, best performance)
$$ LANGUAGE plpgsql;     -- PL/pgSQL (logic: IF, LOOP, variables)
$$ LANGUAGE plpython3u;  -- Python (needs extension)
$$ LANGUAGE plperl;      -- Perl (needs extension)
```

> For most UDFs, use `SQL` for simple ones and `plpgsql` when you need logic.

---

## 1. Scalar UDF — Returns a Single Value

### SQL Language (simplest)

```sql
CREATE OR REPLACE FUNCTION calc_tax(price NUMERIC, rate NUMERIC DEFAULT 0.18)
RETURNS NUMERIC AS $$
    SELECT ROUND(price * rate, 2);
$$ LANGUAGE SQL;

SELECT calc_tax(1000);          -- 180.00
SELECT calc_tax(1000, 0.12);    -- 120.00
SELECT name, price, calc_tax(price) AS tax FROM products;
```

### PL/pgSQL (with logic)

```sql
CREATE OR REPLACE FUNCTION get_grade(score INT)
RETURNS TEXT AS $$
BEGIN
    IF score >= 90 THEN RETURN 'A';
    ELSIF score >= 80 THEN RETURN 'B';
    ELSIF score >= 70 THEN RETURN 'C';
    ELSIF score >= 60 THEN RETURN 'D';
    ELSE RETURN 'F';
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT get_grade(95);   -- A
SELECT get_grade(55);   -- F
SELECT name, marks, get_grade(marks) AS grade FROM students;
```

---

## 2. UDF With DECLARE — Variables

```sql
CREATE OR REPLACE FUNCTION dept_summary(dept_name TEXT)
RETURNS TEXT AS $$
DECLARE
    emp_count INT;
    avg_sal NUMERIC;
    result TEXT;
BEGIN
    SELECT COUNT(*), ROUND(AVG(salary), 2)
    INTO emp_count, avg_sal
    FROM employees
    WHERE department = dept_name;

    IF emp_count = 0 THEN
        RETURN dept_name || ': no employees found';
    END IF;

    result := dept_name || ': ' || emp_count || ' people, avg ₹' || avg_sal;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

SELECT dept_summary('Engineering');
-- Engineering: 5 people, avg ₹92000.00
```

---

## 3. Table-Returning UDF — Returns Multiple Rows + Columns

```sql
CREATE OR REPLACE FUNCTION get_top_earners(n INT)
RETURNS TABLE(emp_name TEXT, emp_salary NUMERIC, emp_dept TEXT) AS $$
BEGIN
    RETURN QUERY
    SELECT name, salary, department
    FROM employees
    ORDER BY salary DESC
    LIMIT n;
END;
$$ LANGUAGE plpgsql;

-- use like a table
SELECT * FROM get_top_earners(3);
SELECT * FROM get_top_earners(5) WHERE emp_dept = 'Engineering';
```

```
 emp_name | emp_salary | emp_dept
----------+------------+-------------
 Diana    | 110000     | Engineering
 Alice    | 95000      | Engineering
 Hank     | 92000      | Engineering
```

---

## 4. SETOF UDF — Returns Set of an Existing Type

```sql
-- returns multiple rows of an existing table type
CREATE OR REPLACE FUNCTION get_active_employees()
RETURNS SETOF employees AS $$
    SELECT * FROM employees WHERE is_active = true;
$$ LANGUAGE SQL;

SELECT * FROM get_active_employees();
SELECT name, salary FROM get_active_employees() WHERE department = 'HR';
```

> RETURNS TABLE = define columns inline. RETURNS SETOF = return rows of an existing table/type.

---

## 5. VOID UDF — No Return Value

```sql
CREATE OR REPLACE FUNCTION log_action(action TEXT, detail TEXT)
RETURNS VOID AS $$
BEGIN
    INSERT INTO audit_log (action, detail, created_at)
    VALUES (action, detail, NOW());
END;
$$ LANGUAGE plpgsql;

SELECT log_action('LOGIN', 'user alice logged in');
-- inserts into audit_log, returns nothing
```

---

## Parameter Modes: IN, OUT, INOUT

```sql
-- IN = input only (default)
-- OUT = output only (alternative to RETURNS)
-- INOUT = both input and output

CREATE OR REPLACE FUNCTION get_stats(
    IN dept TEXT,
    OUT emp_count INT,
    OUT avg_salary NUMERIC,
    OUT max_salary NUMERIC
) AS $$
BEGIN
    SELECT COUNT(*), ROUND(AVG(salary), 2), MAX(salary)
    INTO emp_count, avg_salary, max_salary
    FROM employees
    WHERE department = dept;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM get_stats('Engineering');
```

```
 emp_count | avg_salary | max_salary
-----------+------------+-----------
 5         | 92000.00   | 110000
```

> OUT parameters = automatic RETURNS RECORD. No explicit RETURN needed.

---

## Default Parameter Values

```sql
CREATE OR REPLACE FUNCTION greet(name TEXT, greeting TEXT DEFAULT 'Hello')
RETURNS TEXT AS $$
BEGIN
    RETURN greeting || ', ' || name || '!';
END;
$$ LANGUAGE plpgsql;

SELECT greet('Alice');              -- Hello, Alice!
SELECT greet('Alice', 'Namaste');   -- Namaste, Alice!
```

---

## VARIADIC — Variable Number of Arguments

```sql
CREATE OR REPLACE FUNCTION sum_all(VARIADIC nums INT[])
RETURNS INT AS $$
DECLARE
    total INT := 0;
    n INT;
BEGIN
    FOREACH n IN ARRAY nums LOOP
        total := total + n;
    END LOOP;
    RETURN total;
END;
$$ LANGUAGE plpgsql;

SELECT sum_all(1, 2, 3);          -- 6
SELECT sum_all(10, 20, 30, 40);   -- 100
```

---

## Function Volatility: IMMUTABLE / STABLE / VOLATILE

Tells Postgres how to optimize the function.

```sql
-- IMMUTABLE: same input = always same output. Can be used in indexes.
CREATE FUNCTION add_one(n INT) RETURNS INT AS $$
    SELECT n + 1;
$$ LANGUAGE SQL IMMUTABLE;

-- STABLE: same output within a single query (reads DB, but doesn't modify)
CREATE FUNCTION get_setting(key TEXT) RETURNS TEXT AS $$
    SELECT value FROM settings WHERE name = key;
$$ LANGUAGE SQL STABLE;

-- VOLATILE: can return different results each call, or modifies DB (default)
CREATE FUNCTION log_and_return(n INT) RETURNS INT AS $$
BEGIN
    INSERT INTO call_log (val) VALUES (n);
    RETURN n;
END;
$$ LANGUAGE plpgsql VOLATILE;
```

| Volatility | When to Use | Can Index? |
|-----------|-------------|-----------|
| `IMMUTABLE` | pure math, formatting (no DB access) | Yes |
| `STABLE` | reads DB but no writes | No |
| `VOLATILE` | writes to DB, or uses NOW() (default) | No |

```sql
-- IMMUTABLE functions can be used in expression indexes
CREATE INDEX idx_lower_name ON users(LOWER(name));  -- LOWER is IMMUTABLE
```

---

## STRICT — Auto-Return NULL for NULL Input

```sql
-- without STRICT: you must handle NULL yourself
-- with STRICT: if ANY argument is NULL, function returns NULL automatically

CREATE FUNCTION safe_add(a INT, b INT)
RETURNS INT AS $$
    SELECT a + b;
$$ LANGUAGE SQL STRICT;

SELECT safe_add(5, 3);     -- 8
SELECT safe_add(5, NULL);  -- NULL (auto, no error)
```

---

## SECURITY DEFINER — Run As Function Owner

```sql
-- normally functions run with the CALLER's permissions
-- SECURITY DEFINER runs with the OWNER's permissions (like sudo)

CREATE FUNCTION get_secret_data()
RETURNS TABLE(id INT, secret TEXT)
SECURITY DEFINER AS $$
    SELECT id, secret FROM secrets;
$$ LANGUAGE SQL;

-- now even users without SELECT on 'secrets' table can call this function
```

> Use carefully — like Oracle's `AUTHID DEFINER`. Can be a security risk if function has SQL injection.

---

## DO Block — Anonymous Function (One-Time Use)

```sql
-- run PL/pgSQL code without creating a function
DO $$
DECLARE
    emp_count INT;
BEGIN
    SELECT COUNT(*) INTO emp_count FROM employees;
    RAISE NOTICE 'Total employees: %', emp_count;

    IF emp_count > 100 THEN
        RAISE NOTICE 'Large company!';
    ELSE
        RAISE NOTICE 'Small company';
    END IF;
END;
$$;
```

> DO blocks cannot return values. They're for one-time scripts, migrations, data fixes.

---

## Control Flow in PL/pgSQL

### IF / ELSIF / ELSE

```sql
IF condition THEN
    -- do something
ELSIF other_condition THEN
    -- do something else
ELSE
    -- default
END IF;
```

### FOR Loop

```sql
-- numeric loop
FOR i IN 1..10 LOOP
    RAISE NOTICE 'i = %', i;
END LOOP;

-- reverse loop
FOR i IN REVERSE 10..1 LOOP
    RAISE NOTICE 'i = %', i;
END LOOP;

-- loop over query result
FOR rec IN SELECT * FROM employees WHERE dept = 'Eng' LOOP
    RAISE NOTICE 'Name: %', rec.name;
END LOOP;
```

### WHILE Loop

```sql
WHILE counter < 10 LOOP
    counter := counter + 1;
END LOOP;
```

### LOOP with EXIT

```sql
LOOP
    EXIT WHEN counter >= 10;
    counter := counter + 1;
END LOOP;
```

### FOREACH (array loop)

```sql
FOREACH item IN ARRAY my_array LOOP
    RAISE NOTICE 'Item: %', item;
END LOOP;
```

---

## Exception Handling

```sql
CREATE OR REPLACE FUNCTION safe_insert(emp_name TEXT, emp_salary NUMERIC)
RETURNS TEXT AS $$
BEGIN
    INSERT INTO employees (name, salary) VALUES (emp_name, emp_salary);
    RETURN 'Success';
EXCEPTION
    WHEN unique_violation THEN
        RETURN 'Error: duplicate name';
    WHEN check_violation THEN
        RETURN 'Error: invalid salary';
    WHEN OTHERS THEN
        RETURN 'Error: ' || SQLERRM;
END;
$$ LANGUAGE plpgsql;

SELECT safe_insert('Alice', 90000);  -- Success or error message
```

| Exception | When |
|-----------|------|
| `unique_violation` | duplicate key |
| `check_violation` | CHECK constraint failed |
| `foreign_key_violation` | FK constraint failed |
| `not_null_violation` | NULL in NOT NULL column |
| `division_by_zero` | divide by zero |
| `OTHERS` | catch-all |

---

## RAISE — Debug & Error

```sql
RAISE NOTICE 'Debug: count = %', my_count;     -- info log
RAISE WARNING 'Something unusual: %', detail;   -- warning log
RAISE EXCEPTION 'Cannot proceed: %', reason;    -- stops execution + rollback
```

---

## Drop / Replace / List Functions

```sql
-- drop (must specify parameter types)
DROP FUNCTION calc_tax(NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS calc_tax(NUMERIC, NUMERIC);

-- list all custom functions
SELECT routine_name, routine_type, data_type AS returns
FROM information_schema.routines
WHERE routine_schema = 'public'
ORDER BY routine_name;

-- detailed: show function source code
SELECT prosrc FROM pg_proc WHERE proname = 'calc_tax';
```

---

## UDF in Different Contexts

```sql
-- in SELECT
SELECT name, calc_tax(price) AS tax FROM products;

-- in WHERE
SELECT * FROM students WHERE get_grade(marks) = 'A';

-- in ORDER BY
SELECT * FROM employees ORDER BY get_grade(score) DESC;

-- in CHECK constraint (IMMUTABLE functions only)
ALTER TABLE products ADD CONSTRAINT chk_tax
    CHECK (calc_tax(price) < 10000);

-- in INDEX (IMMUTABLE functions only)
CREATE INDEX idx_grade ON students(get_grade(marks));

-- in DEFAULT value
ALTER TABLE orders ALTER COLUMN order_no
    SET DEFAULT generate_order_number();
```

---

## Function vs Procedure (Postgres 11+)

| Feature | Function | Procedure |
|---------|----------|-----------|
| Keyword | `CREATE FUNCTION` | `CREATE PROCEDURE` |
| Returns value | Yes | No (use OUT params) |
| Called with | `SELECT my_func()` | `CALL my_proc()` |
| Transaction control | No (runs inside caller's txn) | Yes (can COMMIT/ROLLBACK inside) |
| Use in SELECT | Yes | No |
| Use in WHERE | Yes | No |

> Functions = compute and return. Procedures = do work (with transaction control).
> Procedures are covered in folder 14b.

---

## Quick Reference: UDF Patterns

| Pattern | Approach |
|---------|----------|
| Calculate a value | Scalar function, `RETURNS NUMERIC/TEXT/INT` |
| Get a filtered result set | `RETURNS TABLE(...)` or `RETURNS SETOF` |
| Multiple output values | `OUT` parameters |
| Variable arguments | `VARIADIC` |
| One-time script | `DO $$ ... $$;` |
| Pure calculation (indexable) | `IMMUTABLE` |
| Reads DB | `STABLE` |
| Writes DB | `VOLATILE` (default) |
| Auto-NULL on NULL input | `STRICT` |
| Run as owner | `SECURITY DEFINER` |

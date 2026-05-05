# 14 - Functions & Triggers (PL/pgSQL)

---

## Simple SQL Function

```sql
CREATE OR REPLACE FUNCTION calc_tax(price NUMERIC, rate NUMERIC DEFAULT 0.18)
RETURNS NUMERIC AS $$
    SELECT ROUND(price * rate, 2);
$$ LANGUAGE SQL;

SELECT calc_tax(1000);        -- 180.00
SELECT calc_tax(1000, 0.12);  -- 120.00
```

---

## PL/pgSQL Function — With Logic

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

SELECT get_grade(95);  -- A
SELECT get_grade(72);  -- C
```

---

## Function With Variables (DECLARE)

```sql
CREATE OR REPLACE FUNCTION full_summary(dept TEXT)
RETURNS TEXT AS $$
DECLARE
    emp_count INT;
    avg_sal NUMERIC;
    result TEXT;
BEGIN
    SELECT COUNT(*), ROUND(AVG(salary), 2)
    INTO emp_count, avg_sal
    FROM employees WHERE department = dept;

    result := dept || ': ' || emp_count || ' people, avg salary ' || avg_sal;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

SELECT full_summary('Engineering');
-- Engineering: 5 people, avg salary 92000.00
```

---

## Function Returning TABLE

```sql
CREATE OR REPLACE FUNCTION get_top_earners(n INT)
RETURNS TABLE(emp_name TEXT, emp_salary NUMERIC) AS $$
BEGIN
    RETURN QUERY
    SELECT name, salary FROM employees ORDER BY salary DESC LIMIT n;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM get_top_earners(3);
```

```
 emp_name | emp_salary
----------+-----------
 Diana    | 110000
 Alice    | 95000
 Hank     | 92000
```

---

## Function With Loop

```sql
CREATE OR REPLACE FUNCTION factorial(n INT)
RETURNS BIGINT AS $$
DECLARE
    result BIGINT := 1;
BEGIN
    FOR i IN 1..n LOOP
        result := result * i;
    END LOOP;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

SELECT factorial(5);   -- 120
```

---

## RAISE — Debug Messages & Errors

```sql
CREATE OR REPLACE FUNCTION safe_divide(a NUMERIC, b NUMERIC)
RETURNS NUMERIC AS $$
BEGIN
    IF b = 0 THEN
        RAISE EXCEPTION 'Division by zero!';   -- stops execution
    END IF;
    RAISE NOTICE 'Dividing % by %', a, b;      -- debug log (visible in psql)
    RETURN ROUND(a / b, 4);
END;
$$ LANGUAGE plpgsql;

SELECT safe_divide(10, 3);   -- NOTICE: Dividing 10 by 3 -> 3.3333
-- SELECT safe_divide(10, 0);   -- ERROR: Division by zero!
```

| Level | Behavior |
|-------|----------|
| `RAISE NOTICE` | print message, continue |
| `RAISE WARNING` | print warning, continue |
| `RAISE EXCEPTION` | stop execution, rollback |

---

## Drop Function

```sql
DROP FUNCTION calc_tax(NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS calc_tax(NUMERIC, NUMERIC);
```

---

# TRIGGERS

A trigger automatically runs a function when a table event occurs.

---

## Trigger Flow

```
Event (INSERT/UPDATE/DELETE)
    |
    v
BEFORE trigger  -->  can MODIFY the row (change NEW values)
    |
    v
Actual INSERT/UPDATE/DELETE happens
    |
    v
AFTER trigger   -->  can LOG, AUDIT, notify (cannot change the row)
```

---

## BEFORE Trigger — Auto-Set updated_at

```sql
-- step 1: create the trigger function
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();    -- modify the row before it's saved
    RETURN NEW;                -- MUST return NEW for BEFORE triggers
END;
$$ LANGUAGE plpgsql;

-- step 2: attach trigger to table
CREATE TRIGGER trg_updated_at
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
```

Now every INSERT or UPDATE automatically sets `updated_at`.

---

## AFTER Trigger — Audit Trail

```sql
CREATE TABLE products_audit (
    id SERIAL, product_id INT,
    old_price NUMERIC, new_price NUMERIC,
    changed_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION audit_price_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.price IS DISTINCT FROM NEW.price THEN
        INSERT INTO products_audit (product_id, old_price, new_price)
        VALUES (OLD.id, OLD.price, NEW.price);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_price
    AFTER UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION audit_price_change();
```

```sql
UPDATE products SET price = 899 WHERE name = 'Phone';
SELECT * FROM products_audit;
-- product_id=1, old_price=999, new_price=899, changed_at=...
```

---

## Prevent Delete Trigger

```sql
CREATE OR REPLACE FUNCTION prevent_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Deletion not allowed! Use soft delete instead.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_no_delete
    BEFORE DELETE ON products
    FOR EACH ROW EXECUTE FUNCTION prevent_delete();

-- DELETE FROM products WHERE id = 1;  -- ERROR: Deletion not allowed!
```

---

## NEW and OLD References

| Event | NEW | OLD |
|-------|-----|-----|
| INSERT | the new row being inserted | not available |
| UPDATE | the new values | the old values (before update) |
| DELETE | not available | the row being deleted |

```sql
-- in a trigger function:
NEW.name     -- the new value of 'name' column
OLD.name     -- the previous value of 'name' column
TG_OP        -- 'INSERT', 'UPDATE', or 'DELETE'
TG_TABLE_NAME -- name of the table that fired the trigger
```

---

## ROW-Level vs STATEMENT-Level

```sql
-- ROW level: fires once PER ROW affected
CREATE TRIGGER trg_per_row AFTER UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION my_func();

-- STATEMENT level: fires ONCE per statement (regardless of how many rows)
CREATE TRIGGER trg_per_stmt AFTER UPDATE ON products
    FOR EACH STATEMENT EXECUTE FUNCTION my_func();
```

| Type | When | Use Case |
|------|------|----------|
| FOR EACH ROW | once per affected row | audit each row change |
| FOR EACH STATEMENT | once per SQL statement | log that "an update happened" |

---

## List / Drop Triggers

```sql
-- list triggers on a table
SELECT trigger_name, event_manipulation, action_timing
FROM information_schema.triggers WHERE event_object_table = 'products';

-- drop trigger
DROP TRIGGER trg_no_delete ON products;
DROP TRIGGER IF EXISTS trg_no_delete ON products;
```

---

## Common Trigger Patterns

| Pattern | Trigger Type | Example |
|---------|-------------|---------|
| Auto-set timestamp | BEFORE INSERT/UPDATE | `NEW.updated_at = NOW()` |
| Audit logging | AFTER UPDATE/DELETE | log old values to audit table |
| Prevent action | BEFORE DELETE | RAISE EXCEPTION |
| Auto-uppercase | BEFORE INSERT | `NEW.name = UPPER(NEW.name)` |
| Prevent salary decrease | BEFORE UPDATE | check `NEW.salary >= OLD.salary` |
| Cascade calculation | AFTER INSERT | update parent table's total |

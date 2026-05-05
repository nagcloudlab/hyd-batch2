# 02 - String Functions (Postgres)

---

## Case Conversion

```sql
SELECT UPPER('hello');          -- HELLO
SELECT LOWER('HELLO');          -- hello
SELECT INITCAP('john doe');     -- John Doe  (Postgres only)
```

---

## Length

```sql
SELECT LENGTH('Hyderabad');     -- 9
SELECT CHAR_LENGTH('Hyderabad');-- 9 (same for ASCII)
SELECT OCTET_LENGTH('Hyderabad'); -- 9 (byte count, differs for unicode)
```

---

## Concatenation

```sql
-- || operator (Postgres preferred)
SELECT 'Hello' || ' ' || 'World';           -- Hello World

-- CONCAT function (NULL-safe, treats NULL as empty)
SELECT CONCAT('Hello', NULL, 'World');       -- HelloWorld

-- CONCAT_WS = concat with separator
SELECT CONCAT_WS(', ', 'Hyd', 'Telangana', 'India');  -- Hyd, Telangana, India
```

> `'hello' || NULL` = NULL. But `CONCAT('hello', NULL)` = 'hello'. Choose wisely.

---

## Substring / Left / Right

```sql
-- SUBSTRING(string FROM start FOR length)
SELECT SUBSTRING('Hyderabad' FROM 1 FOR 3);   -- Hyd
SELECT SUBSTRING('Hyderabad', 4, 5);          -- eraba

-- LEFT / RIGHT
SELECT LEFT('Hyderabad', 3);    -- Hyd
SELECT RIGHT('Hyderabad', 4);   -- abad
```

---

## Trim

```sql
SELECT TRIM('  hello  ');              -- 'hello'
SELECT LTRIM('  hello  ');             -- 'hello  '
SELECT RTRIM('  hello  ');             -- '  hello'
SELECT TRIM(BOTH '.' FROM '...hi...');-- 'hi'
SELECT TRIM(LEADING '0' FROM '00042');-- '42'
```

---

## Replace

```sql
SELECT REPLACE('2024-01-15', '-', '/');   -- 2024/01/15
SELECT REPLACE('hello world', 'world', 'postgres'); -- hello postgres
```

---

## Position / Strpos — Find Substring

```sql
SELECT POSITION('@' IN 'user@gmail.com');  -- 5
SELECT STRPOS('user@gmail.com', '@');      -- 5  (Postgres style)
```

> Returns 0 if not found (not -1 like most languages).

---

## SPLIT_PART — Split and Pick (Postgres)

```sql
-- SPLIT_PART(string, delimiter, part_number)
SELECT SPLIT_PART('user@gmail.com', '@', 1);  -- user
SELECT SPLIT_PART('user@gmail.com', '@', 2);  -- gmail.com

SELECT SPLIT_PART('Hyd, Telangana, India', ', ', 2);  -- Telangana
```

> Part numbers are 1-based. Returns empty string if part doesn't exist.

---

## LPAD / RPAD — Padding

```sql
SELECT LPAD('42', 5, '0');     -- 00042
SELECT RPAD('hi', 10, '.');   -- hi........

-- common: format employee IDs
SELECT LPAD(id::TEXT, 6, '0') AS emp_id FROM employees;
-- 000001, 000002, ...
```

---

## REPEAT / REVERSE

```sql
SELECT REPEAT('*', 5);     -- *****
SELECT REVERSE('hello');    -- olleh
```

---

## Regular Expressions

```sql
-- ~ match (case sensitive), ~* match (case insensitive)
SELECT 'Hello' ~ 'ell';       -- true
SELECT 'Hello' ~* 'ELL';      -- true

-- REGEXP_REPLACE
SELECT REGEXP_REPLACE('Ph: 91-987-654-3210', '[^0-9]', '', 'g');
-- 919876543210  (digits only)

-- REGEXP_REPLACE: collapse multiple spaces
SELECT REGEXP_REPLACE('hello    world', '\s+', ' ', 'g');
-- hello world
```

> The `'g'` flag = global (replace all). Without it, only the first match is replaced.

---

## FORMAT — Printf-Style (Postgres)

```sql
SELECT FORMAT('Hello, %s! You have %s items.', 'Alice', 5);
-- Hello, Alice! You have 5 items.

-- %s = string, %I = identifier (quoted), %L = literal (safe)
SELECT FORMAT('SELECT * FROM %I WHERE name = %L', 'users', 'Alice');
-- SELECT * FROM users WHERE name = 'Alice'
```

---

## Quick Real-World: Clean Messy Data

```sql
SELECT
    INITCAP(TRIM(first_name)) AS first_name,     -- fix case + spaces
    LOWER(TRIM(email)) AS email,                   -- normalize email
    REGEXP_REPLACE(phone, '[^0-9]', '', 'g') AS phone,  -- digits only
    LEFT(first_name, 1) || LOWER(last_name) AS username  -- jdoe style
FROM raw_customers;
```

---

## Cheat Sheet

| Function | Example | Result |
|----------|---------|--------|
| `UPPER('hi')` | | `HI` |
| `LOWER('HI')` | | `hi` |
| `INITCAP('john doe')` | | `John Doe` |
| `LENGTH('abc')` | | `3` |
| `CONCAT('a','b')` | | `ab` |
| `SUBSTRING('hello',2,3)` | | `ell` |
| `LEFT('hello',2)` | | `he` |
| `RIGHT('hello',2)` | | `lo` |
| `TRIM('  hi  ')` | | `hi` |
| `REPLACE('abc','b','x')` | | `axc` |
| `SPLIT_PART('a-b-c','-',2)` | | `b` |
| `LPAD('5',3,'0')` | | `005` |
| `REVERSE('abc')` | | `cba` |

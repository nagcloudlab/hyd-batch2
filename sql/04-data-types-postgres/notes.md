# 04 - Postgres Data Types

---

## Numeric Types

```sql
CREATE TABLE example (
    a SMALLINT,           -- -32768 to 32767 (2 bytes)
    b INTEGER,            -- -2 billion to 2 billion (4 bytes)
    c BIGINT,             -- very large (8 bytes)
    d NUMERIC(10,2),      -- exact: 10 total digits, 2 after decimal (use for money!)
    e REAL,               -- 4-byte float (AVOID for money: 0.1 + 0.2 != 0.3)
    f DOUBLE PRECISION    -- 8-byte float
);
```

```sql
-- proof that FLOAT is bad for money
SELECT 0.1::REAL + 0.2::REAL;       -- 0.30000001  (wrong!)
SELECT 0.1::NUMERIC + 0.2::NUMERIC; -- 0.3          (correct)
```

> Rule: NUMERIC for money, REAL/FLOAT only for science/statistics.

---

## SERIAL vs IDENTITY (Auto-increment)

```sql
-- OLD way (still works, but legacy)
CREATE TABLE old_style (
    id SERIAL PRIMARY KEY,
    name TEXT
);

-- MODERN way (Postgres 10+, SQL standard)
CREATE TABLE new_style (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT
);
```

> SERIAL creates a hidden sequence that isn't dropped with the table. IDENTITY is cleaner.

---

## Text Types

```sql
CREATE TABLE example (
    a CHAR(10),       -- fixed length, padded with spaces
    b VARCHAR(100),   -- variable length with limit
    c TEXT            -- unlimited length (Postgres preferred!)
);
```

> In Postgres, TEXT and VARCHAR have identical performance. Prefer TEXT.

---

## Boolean

```sql
-- valid true values: true, 't', 'yes', 'y', '1', 'on'
-- valid false values: false, 'f', 'no', 'n', '0', 'off'

SELECT true::BOOLEAN, 'yes'::BOOLEAN, '1'::BOOLEAN;  -- all true

-- querying booleans
SELECT * FROM users WHERE is_active;          -- shorthand for = true
SELECT * FROM users WHERE NOT is_active;      -- shorthand for = false
```

---

## ARRAY — Store Multiple Values

```sql
-- create with array
CREATE TABLE students (
    name TEXT,
    grades INT[],
    tags TEXT[]
);

-- insert arrays
INSERT INTO students VALUES ('Alice', ARRAY[90, 85, 92], '{sql,python}');
INSERT INTO students VALUES ('Bob', '{78, 88, 95}', ARRAY['java','react']);

-- access element (1-based!)
SELECT name, grades[1] AS first_grade FROM students;

-- check if array contains a value
SELECT * FROM students WHERE 'python' = ANY(tags);

-- array contains all of these
SELECT * FROM students WHERE tags @> ARRAY['sql', 'python'];

-- array length
SELECT name, array_length(grades, 1) FROM students;

-- expand array to rows
SELECT name, UNNEST(grades) AS grade FROM students;
```

---

## ENUM — Fixed Set of Values

```sql
CREATE TYPE status AS ENUM ('pending', 'approved', 'rejected');

CREATE TABLE applications (
    id SERIAL PRIMARY KEY,
    applicant TEXT,
    state status DEFAULT 'pending'
);

INSERT INTO applications (applicant) VALUES ('Alice');        -- pending
INSERT INTO applications (applicant, state) VALUES ('Bob', 'approved');
-- INSERT INTO applications (applicant, state) VALUES ('X', 'unknown'); -- ERROR!
```

> Warning: You can ADD values to an enum but CANNOT remove them.

---

## UUID — Unique Identifiers

```sql
-- built-in (Postgres 13+)
SELECT gen_random_uuid();  -- a1b2c3d4-e5f6-7890-abcd-ef1234567890

-- with extension (older Postgres)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SELECT uuid_generate_v4();

-- use as primary key
CREATE TABLE api_tokens (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    token_name TEXT
);
INSERT INTO api_tokens (token_name) VALUES ('production');
```

> Use UUID for public-facing IDs. Don't expose sequential integers in URLs.

---

## JSONB — Semi-Structured Data

```sql
CREATE TABLE events (
    id SERIAL PRIMARY KEY,
    payload JSONB
);

INSERT INTO events (payload) VALUES ('{"type": "click", "page": "/home", "count": 5}');

-- access fields
SELECT payload -> 'type' FROM events;        -- "click" (JSON type)
SELECT payload ->> 'type' FROM events;       -- click   (TEXT type)
SELECT (payload ->> 'count')::INT FROM events; -- 5     (as integer)
```

> Full JSONB coverage in folder 15. Always prefer JSONB over JSON.

---

## Special Types

```sql
-- INET: IP addresses
SELECT '192.168.1.1'::INET;
SELECT '10.0.0.5'::INET << '10.0.0.0/8'::CIDR;  -- true (is in subnet)

-- DATE RANGE
SELECT '[2024-01-01, 2024-12-31]'::DATERANGE;

-- BYTEA: binary data
SELECT '\xDEADBEEF'::BYTEA;
```

---

## Type Casting

```sql
-- Postgres :: syntax (preferred)
SELECT '42'::INTEGER;
SELECT 42::TEXT;
SELECT '2024-01-15'::DATE;
SELECT 'true'::BOOLEAN;

-- SQL standard CAST syntax
SELECT CAST('42' AS INTEGER);
SELECT CAST('2024-01-15' AS DATE);
```

---

## Cheat Sheet: Which Type to Use?

| Data | Use | Don't Use |
|------|-----|-----------|
| Names, text | `TEXT` | VARCHAR (no benefit in PG) |
| Money | `NUMERIC(10,2)` | REAL, FLOAT |
| Auto-increment ID | `GENERATED ALWAYS AS IDENTITY` | SERIAL (legacy) |
| Public-facing ID | `UUID` | SERIAL (exposes count) |
| True/False | `BOOLEAN` | INT 0/1 |
| Dates | `DATE` | TEXT |
| Timestamps | `TIMESTAMPTZ` | TIMESTAMP (no timezone) |
| Tags, multi-select | `TEXT[]` (array) | comma-separated TEXT |
| Flexible metadata | `JSONB` | JSON (slower, no index) |

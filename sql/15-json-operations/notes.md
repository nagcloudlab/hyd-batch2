# 15 - JSON / JSONB Operations (Postgres)

> Always use JSONB, not JSON. JSONB is binary, indexable, and faster.

---

## JSON vs JSONB

| Feature | JSON | JSONB |
|---------|------|-------|
| Storage | text as-is | binary decomposed |
| Duplicate keys | allowed | last one wins |
| Key order | preserved | not preserved |
| Indexable (GIN) | No | Yes |
| Speed | slower | faster |

---

## Inserting JSONB

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name TEXT,
    profile JSONB
);

INSERT INTO users (name, profile) VALUES
('Alice', '{"age": 28, "city": "Hyderabad", "skills": ["SQL", "Python"]}'),
('Bob',   '{"age": 32, "city": "Mumbai", "skills": ["Java", "React", "Node"]}');
```

---

## Access Operators: -> vs ->>

```sql
-- -> returns JSON type
SELECT profile -> 'city' FROM users;          -- "Hyderabad" (with quotes)

-- ->> returns TEXT type (usually what you want)
SELECT profile ->> 'city' FROM users;         -- Hyderabad (no quotes)

-- cast to integer
SELECT (profile ->> 'age')::INT FROM users;   -- 28
```

| Operator | Returns | Example |
|----------|---------|---------|
| `->` | JSON | `profile -> 'city'` -> `"Hyderabad"` |
| `->>` | TEXT | `profile ->> 'city'` -> `Hyderabad` |
| `#>` | JSON (by path) | `profile #> '{address,street}'` |
| `#>>` | TEXT (by path) | `profile #>> '{address,street}'` |

---

## Nested Access

```sql
-- nested object: profile = {"address": {"street": "MG Road", "pin": "500001"}}
SELECT profile -> 'address' ->> 'street' FROM users;      -- MG Road
SELECT profile #>> '{address, street}' FROM users;          -- MG Road

-- array element (0-based)
SELECT profile -> 'skills' -> 0 FROM users;    -- "SQL"
SELECT profile -> 'skills' ->> 0 FROM users;   -- SQL
```

---

## @> Containment — "Does JSON Contain This?"

```sql
-- find users in Hyderabad
SELECT * FROM users WHERE profile @> '{"city": "Hyderabad"}';

-- find users with Python skill
SELECT * FROM users WHERE profile @> '{"skills": ["Python"]}';

-- find active users
SELECT * FROM users WHERE profile @> '{"active": true}';
```

> @> is the most important JSONB operator. Index it with GIN.

---

## ? Key Existence — "Does Key Exist?"

```sql
SELECT * FROM users WHERE profile ? 'age';         -- has 'age' key
SELECT * FROM users WHERE profile ?| ARRAY['age', 'phone'];  -- has ANY of these
SELECT * FROM users WHERE profile ?& ARRAY['age', 'city'];   -- has ALL of these
```

---

## Expand Arrays to Rows

```sql
-- one row per skill
SELECT name, skill
FROM users, jsonb_array_elements_text(profile -> 'skills') AS skill;
```

```
 name  | skill
-------+--------
 Alice | SQL
 Alice | Python
 Bob   | Java
 Bob   | React
 Bob   | Node
```

```sql
-- array length
SELECT name, jsonb_array_length(profile -> 'skills') AS skill_count FROM users;
```

---

## Expand Object to Key-Value Rows

```sql
SELECT name, key, value
FROM users, jsonb_each_text(profile) AS kv(key, value)
WHERE name = 'Alice';
```

```
 name  | key    | value
-------+--------+------------------
 Alice | age    | 28
 Alice | city   | Hyderabad
 Alice | skills | ["SQL","Python"]
```

---

## Modify JSONB

```sql
-- jsonb_set: update a value
UPDATE users SET profile = jsonb_set(profile, '{city}', '"Pune"')
WHERE name = 'Alice';

-- jsonb_set: add a new key
UPDATE users SET profile = jsonb_set(profile, '{email}', '"alice@test.com"')
WHERE name = 'Alice';

-- || merge: add/overwrite multiple keys
UPDATE users SET profile = profile || '{"phone": "9876543210", "verified": true}'
WHERE name = 'Bob';

-- - delete a key
UPDATE users SET profile = profile - 'verified' WHERE name = 'Bob';

-- #- delete by path
UPDATE users SET profile = profile #- '{skills, 0}' WHERE name = 'Alice';
-- removes first skill
```

| Operation | Syntax | Example |
|-----------|--------|---------|
| Update/add key | `jsonb_set(col, path, value)` | `jsonb_set(profile, '{city}', '"Pune"')` |
| Merge keys | `col \|\| '{"k":"v"}'` | `profile \|\| '{"active":true}'` |
| Delete key | `col - 'key'` | `profile - 'active'` |
| Delete by path | `col #- '{path}'` | `profile #- '{skills,0}'` |

---

## Build JSON From SQL

```sql
-- build object from columns
SELECT jsonb_build_object('id', id, 'name', name, 'city', profile ->> 'city')
FROM users;
-- {"id": 1, "name": "Alice", "city": "Hyderabad"}

-- aggregate rows into JSON array
SELECT jsonb_agg(name) FROM users;
-- ["Alice", "Bob"]

-- full API-style response
SELECT jsonb_build_object(
    'total', (SELECT COUNT(*) FROM users),
    'users', (SELECT jsonb_agg(jsonb_build_object('name', name, 'city', profile ->> 'city')) FROM users)
);
```

---

## JSON to Table Row

```sql
SELECT name, r.*
FROM users, jsonb_to_record(profile) AS r(age INT, city TEXT);
```

```
 name  | age | city
-------+-----+-----------
 Alice | 28  | Hyderabad
 Bob   | 32  | Mumbai
```

---

## GIN Index on JSONB

```sql
-- index entire JSONB column
CREATE INDEX idx_profile ON users USING GIN(profile);

-- now these are fast:
SELECT * FROM users WHERE profile @> '{"city": "Mumbai"}';
SELECT * FROM users WHERE profile ? 'email';

-- index specific path
CREATE INDEX idx_city ON users((profile ->> 'city'));
-- fast: WHERE profile ->> 'city' = 'Mumbai'
```

---

## Quick Reference

| Need | Use |
|------|-----|
| Get text value | `->>` |
| Get JSON value | `->` |
| Nested path text | `#>>` |
| Check if contains | `@>` |
| Check key exists | `?` |
| Update a key | `jsonb_set()` |
| Add keys | `\|\|` merge |
| Delete a key | `-` |
| Array to rows | `jsonb_array_elements_text()` |
| Object to rows | `jsonb_each_text()` |
| Build JSON | `jsonb_build_object()` |
| Aggregate to JSON | `jsonb_agg()` |
| Index for search | `CREATE INDEX USING GIN` |

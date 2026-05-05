# 13 - Advanced Constraints

---

## DEFAULT Values

```sql
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    status TEXT DEFAULT 'pending',
    priority INT DEFAULT 3,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO tasks (title) VALUES ('Fix bug');
-- status='pending', priority=3, created_at=now()  (all defaults applied)
```

---

## CHECK Constraints — Validate Values

```sql
-- single column check
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    price NUMERIC CHECK (price > 0),
    discount NUMERIC CHECK (discount >= 0 AND discount <= 100),
    stock INT CHECK (stock >= 0)
);

INSERT INTO products (name, price) VALUES ('Phone', 999);     -- OK
-- INSERT INTO products (name, price) VALUES ('Bad', -10);     -- ERROR: price > 0
```

```sql
-- multi-column check (named constraint)
CREATE TABLE products (
    price NUMERIC,
    sale_price NUMERIC,
    CONSTRAINT valid_sale CHECK (sale_price IS NULL OR sale_price < price)
);
```

> Name your constraints! Makes error messages readable.

---

## ON DELETE Actions (Foreign Key)

What happens when you delete a parent row?

```sql
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES customers(id) ON DELETE CASCADE
);
```

| Action | What Happens | Example |
|--------|-------------|---------|
| `RESTRICT` (default) | block delete if children exist | can't delete customer with orders |
| `CASCADE` | auto-delete children | delete customer -> deletes all their orders |
| `SET NULL` | set FK to NULL | delete author -> articles.author_id = NULL |
| `SET DEFAULT` | set FK to default value | rarely used |
| `NO ACTION` | same as RESTRICT (checked at end) | subtle timing difference |

```sql
-- CASCADE: delete customer = delete all their orders + order items
CREATE TABLE orders (
    customer_id INT REFERENCES customers(id) ON DELETE CASCADE
);
CREATE TABLE order_items (
    order_id INT REFERENCES orders(id) ON DELETE CASCADE
);

DELETE FROM customers WHERE id = 1;
-- customers row gone, all orders gone, all order items gone!
```

> CASCADE is powerful but dangerous — can silently delete thousands of rows.

```sql
-- SET NULL: keep the article, clear the author
CREATE TABLE articles (
    author_id INT REFERENCES authors(id) ON DELETE SET NULL
);

DELETE FROM authors WHERE id = 1;
-- articles still exist, author_id = NULL
```

---

## ON UPDATE CASCADE

```sql
CREATE TABLE items (
    category_code VARCHAR(10) REFERENCES categories(code) ON UPDATE CASCADE
);

-- update category code, items auto-update
UPDATE categories SET code = 'ELECTRONICS' WHERE code = 'ELEC';
-- items.category_code automatically changes from 'ELEC' to 'ELECTRONICS'
```

---

## Adding / Dropping Constraints on Existing Tables

```sql
-- add CHECK
ALTER TABLE employees ADD CONSTRAINT chk_age CHECK (age >= 18);

-- add UNIQUE
ALTER TABLE employees ADD CONSTRAINT uniq_email UNIQUE (email);

-- add NOT NULL
ALTER TABLE employees ALTER COLUMN email SET NOT NULL;

-- add DEFAULT
ALTER TABLE employees ALTER COLUMN status SET DEFAULT 'active';

-- add FOREIGN KEY
ALTER TABLE orders ADD CONSTRAINT fk_customer
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE;

-- drop constraints
ALTER TABLE employees DROP CONSTRAINT chk_age;
ALTER TABLE employees ALTER COLUMN email DROP NOT NULL;
ALTER TABLE employees ALTER COLUMN status DROP DEFAULT;
```

---

## DEFERRABLE Constraints — Check at COMMIT

Normally, FK constraints are checked immediately. DEFERRABLE lets you check at COMMIT.

```sql
CREATE TABLE child (
    parent_id INT REFERENCES parent(id)
        DEFERRABLE INITIALLY DEFERRED
);

BEGIN;
    INSERT INTO child VALUES (1, 100);   -- parent 100 doesn't exist YET
    INSERT INTO parent VALUES (100);     -- now it does
COMMIT;  -- FK checked here — passes!
```

> Useful for bulk imports where insert order doesn't match FK dependencies.

---

## EXCLUDE Constraint — Prevent Overlaps (Postgres Only)

Prevents overlapping values. Perfect for scheduling/booking.

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE room_bookings (
    room TEXT NOT NULL,
    during TSTZRANGE NOT NULL,
    EXCLUDE USING GIST (room WITH =, during WITH &&)
);

-- book Room A: 10am to 12pm
INSERT INTO room_bookings VALUES ('Room A', '[2024-06-01 10:00, 2024-06-01 12:00)');

-- book Room A: 1pm to 3pm (OK, no overlap)
INSERT INTO room_bookings VALUES ('Room A', '[2024-06-01 13:00, 2024-06-01 15:00)');

-- book Room A: 11am to 1pm (ERROR! overlaps with first booking)
-- INSERT INTO room_bookings VALUES ('Room A', '[2024-06-01 11:00, 2024-06-01 13:00)');

-- Room B at same time is OK (different room)
INSERT INTO room_bookings VALUES ('Room B', '[2024-06-01 11:00, 2024-06-01 13:00)');
```

> `&&` means "overlaps". The constraint says: same room cannot have overlapping time ranges.

---

## List All Constraints on a Table

```sql
SELECT conname AS name,
    contype AS type,  -- p=PK, f=FK, u=unique, c=check, x=exclude
    pg_get_constraintdef(oid) AS definition
FROM pg_constraint
WHERE conrelid = 'products'::regclass;
```

---

## Quick Reference

| Constraint | Purpose | Example |
|-----------|---------|---------|
| `NOT NULL` | no NULLs allowed | email TEXT NOT NULL |
| `UNIQUE` | no duplicates | email TEXT UNIQUE |
| `PRIMARY KEY` | NOT NULL + UNIQUE (one per table) | id INT PRIMARY KEY |
| `CHECK` | validate values | CHECK (age >= 18) |
| `FOREIGN KEY` | reference another table | REFERENCES users(id) |
| `DEFAULT` | auto-fill value | status TEXT DEFAULT 'active' |
| `EXCLUDE` | prevent overlaps | EXCLUDE USING GIST (...) |

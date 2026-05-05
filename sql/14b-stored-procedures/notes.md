# 14b - Stored Procedures (Postgres 11+)

> Procedures were added in Postgres 11. Before that, everything was a function.

---

## Function vs Procedure

| Feature | Function | Procedure |
|---------|----------|-----------|
| Keyword | `CREATE FUNCTION` | `CREATE PROCEDURE` |
| Call with | `SELECT my_func()` | `CALL my_proc()` |
| Returns value? | Yes (RETURNS type) | No (use OUT params for output) |
| Use in SELECT/WHERE? | Yes | No |
| Transaction control? | No | Yes (COMMIT/ROLLBACK inside) |
| Use case | compute + return | do work, multi-step operations |

> Key difference: **Procedures can COMMIT/ROLLBACK inside.** Functions cannot.

---

## Basic Procedure — No Return

```sql
CREATE OR REPLACE PROCEDURE log_event(p_action TEXT, p_detail TEXT)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO audit_log (action, detail, created_at)
    VALUES (p_action, p_detail, NOW());
END;
$$;

-- call it (NOT select!)
CALL log_event('USER_LOGIN', 'alice logged in');
```

---

## Procedure With OUT Parameters

```sql
CREATE OR REPLACE PROCEDURE transfer_money(
    IN from_acct TEXT,
    IN to_acct TEXT,
    IN amount NUMERIC,
    OUT status TEXT
)
LANGUAGE plpgsql AS $$
DECLARE
    from_balance NUMERIC;
BEGIN
    SELECT balance INTO from_balance FROM accounts WHERE holder = from_acct;

    IF from_balance < amount THEN
        status := 'FAILED: insufficient funds';
        RETURN;
    END IF;

    UPDATE accounts SET balance = balance - amount WHERE holder = from_acct;
    UPDATE accounts SET balance = balance + amount WHERE holder = to_acct;

    status := 'SUCCESS: transferred ' || amount;
END;
$$;

CALL transfer_money('Alice', 'Bob', 1000, NULL);
```

---

## Procedure With Transaction Control (The Big Feature)

Functions run inside the caller's transaction. Procedures can manage their own.

```sql
CREATE OR REPLACE PROCEDURE batch_process()
LANGUAGE plpgsql AS $$
DECLARE
    rec RECORD;
    batch_count INT := 0;
BEGIN
    FOR rec IN SELECT id FROM pending_jobs ORDER BY id LOOP
        -- process each job
        UPDATE pending_jobs SET status = 'done' WHERE id = rec.id;
        batch_count := batch_count + 1;

        -- commit every 100 rows (keeps transaction small)
        IF batch_count % 100 = 0 THEN
            COMMIT;   -- this is ONLY possible in a procedure!
            RAISE NOTICE 'Committed % rows', batch_count;
        END IF;
    END LOOP;

    COMMIT;  -- final commit
    RAISE NOTICE 'Total processed: %', batch_count;
END;
$$;

CALL batch_process();
```

> In a function, `COMMIT` inside would cause an error. This is the #1 reason procedures exist.

---

## When to Use Procedure vs Function

| Scenario | Use |
|----------|-----|
| Calculate a value, return it | Function |
| Use result in SELECT/WHERE | Function |
| Multi-step operation (transfer, batch) | Procedure |
| Need COMMIT/ROLLBACK inside | Procedure |
| Void operation (logging, cleanup) | Either (procedure is cleaner) |

---

## Drop Procedure

```sql
DROP PROCEDURE transfer_money(TEXT, TEXT, NUMERIC);
DROP PROCEDURE IF EXISTS transfer_money(TEXT, TEXT, NUMERIC);
```

---

## Key Points

- Procedures use `CALL`, not `SELECT`
- Procedures can't be used in queries (`SELECT my_proc()` = error)
- COMMIT/ROLLBACK inside = only in procedures
- OUT params work but are clunky — many teams still prefer functions
- If you don't need transaction control, a VOID function works just as well

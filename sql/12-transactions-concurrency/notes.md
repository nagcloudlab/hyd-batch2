# 12 - Transactions & Concurrency

---

## What is a Transaction?

A group of SQL statements that execute as ONE unit — all succeed or all fail.

```sql
BEGIN;
    UPDATE accounts SET balance = balance - 1000 WHERE holder = 'Alice';
    UPDATE accounts SET balance = balance + 1000 WHERE holder = 'Bob';
COMMIT;   -- both succeed together
```

If anything fails between BEGIN and COMMIT, nothing is saved.

---

## BEGIN / COMMIT / ROLLBACK

```sql
-- success path
BEGIN;
    INSERT INTO orders (customer_id, total) VALUES (1, 500);
    INSERT INTO audit_log (action) VALUES ('order created');
COMMIT;   -- saves both

-- failure path
BEGIN;
    UPDATE accounts SET balance = balance - 50000 WHERE holder = 'Alice';
    -- oops, Alice doesn't have 50000
ROLLBACK; -- undo everything, balance unchanged
```

---

## Autocommit

Without BEGIN, every statement is its own transaction — auto-committed immediately.

```sql
-- this is committed instantly (no BEGIN)
UPDATE employees SET salary = salary + 1000 WHERE id = 1;
-- can't ROLLBACK this!
```

---

## SAVEPOINT — Partial Rollback

```sql
BEGIN;
    UPDATE accounts SET balance = balance - 500 WHERE holder = 'Alice';
    INSERT INTO audit_log (action) VALUES ('debit Alice 500');

    SAVEPOINT sp1;

    UPDATE accounts SET balance = balance - 999999 WHERE holder = 'Bob';
    -- this is wrong, rollback only this part

    ROLLBACK TO SAVEPOINT sp1;
    -- Bob's update is undone. Alice's debit + audit log are kept.

    UPDATE accounts SET balance = balance + 500 WHERE holder = 'Charlie';
COMMIT;  -- Alice debited, Charlie credited, Bob untouched
```

---

## ACID Properties

| Property | Meaning | Example |
|----------|---------|---------|
| **A**tomicity | all or nothing | transfer: both debit+credit or neither |
| **C**onsistency | DB stays valid | can't violate constraints mid-transaction |
| **I**solation | concurrent txns don't interfere | two transfers don't corrupt balance |
| **D**urability | committed data survives crashes | power loss after COMMIT = data safe |

---

## Isolation Levels

```sql
BEGIN TRANSACTION ISOLATION LEVEL READ COMMITTED;    -- default
BEGIN TRANSACTION ISOLATION LEVEL REPEATABLE READ;
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;      -- strictest

SHOW transaction_isolation;  -- check current level
```

| Level | Sees other's uncommitted? | Sees other's committed mid-txn? | Phantom rows? |
|-------|--------------------------|-------------------------------|--------------|
| READ COMMITTED (default) | No | Yes | Yes |
| REPEATABLE READ | No | No (snapshot from start) | No* |
| SERIALIZABLE | No | No | No |

> Postgres is safe: even READ UNCOMMITTED behaves like READ COMMITTED (no dirty reads ever).

**When to use what:**
- READ COMMITTED: most web apps (default, fine for 99% of cases)
- REPEATABLE READ: reports that need consistent snapshot
- SERIALIZABLE: financial systems, inventory (prevents all anomalies)

---

## Row Locking: FOR UPDATE

```sql
BEGIN;
    -- lock Alice's row — no one else can modify until we COMMIT
    SELECT * FROM accounts WHERE holder = 'Alice' FOR UPDATE;

    -- safely update
    UPDATE accounts SET balance = balance - 100 WHERE holder = 'Alice';
COMMIT;
```

| Lock Mode | Behavior |
|-----------|----------|
| `FOR UPDATE` | lock row, others wait |
| `FOR UPDATE NOWAIT` | lock row, others get error immediately |
| `FOR UPDATE SKIP LOCKED` | skip already-locked rows (job queue pattern!) |
| `FOR SHARE` | shared lock (multiple readers OK, no writers) |

---

## Job Queue Pattern with SKIP LOCKED

```sql
-- worker picks next available job (skips jobs being processed by others)
BEGIN;
    UPDATE job_queue SET status = 'processing', worker = 'w1'
    WHERE id = (
        SELECT id FROM job_queue
        WHERE status = 'pending'
        ORDER BY id
        LIMIT 1
        FOR UPDATE SKIP LOCKED   -- skip rows locked by other workers
    ) RETURNING *;
COMMIT;
```

> Multiple workers can run this simultaneously — each gets a different job.

---

## Deadlocks

Two transactions each waiting for the other's locked row.

```
Txn A: locks row 1, wants row 2
Txn B: locks row 2, wants row 1
= DEADLOCK!
```

Postgres auto-detects deadlocks and kills one transaction.

**Prevention:** always lock rows in the same order (e.g., by ID ascending).

---

## Monitor Locks & Queries

```sql
-- see active queries
SELECT pid, state, LEFT(query, 60) AS query,
       NOW() - query_start AS duration
FROM pg_stat_activity WHERE state != 'idle';

-- kill a long-running query
SELECT pg_cancel_backend(12345);     -- graceful
SELECT pg_terminate_backend(12345);  -- force
```

---

## Key Takeaways

- Wrap related operations in `BEGIN...COMMIT`
- Use `SAVEPOINT` for partial rollback within a transaction
- Keep transactions SHORT — long transactions block VACUUM and other operations
- Use `FOR UPDATE SKIP LOCKED` for job queues
- Postgres default (READ COMMITTED) is safe for most applications
- Deadlocks are auto-resolved but design to avoid them (consistent lock order)

-- ============================================
-- 12 - Transactions & Concurrency Practice
-- ============================================

DROP TABLE IF EXISTS bank_accounts, audit_log;

CREATE TABLE bank_accounts (
    id SERIAL PRIMARY KEY,
    holder TEXT NOT NULL,
    balance NUMERIC(12,2) NOT NULL CHECK (balance >= 0)
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    action TEXT,
    details TEXT,
    ts TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO bank_accounts (holder, balance) VALUES
('Alice', 10000), ('Bob', 5000), ('Charlie', 3000);

-- ============================================
-- Basic Transaction: BEGIN / COMMIT
-- ============================================

-- transfer 2000 from Alice to Bob (all-or-nothing)
BEGIN;
    UPDATE bank_accounts SET balance = balance - 2000 WHERE holder = 'Alice';
    UPDATE bank_accounts SET balance = balance + 2000 WHERE holder = 'Bob';
    INSERT INTO audit_log (action, details) VALUES ('transfer', 'Alice -> Bob: 2000');
COMMIT;

SELECT * FROM bank_accounts;
SELECT * FROM audit_log;

-- ============================================
-- ROLLBACK — undo everything
-- ============================================

BEGIN;
    UPDATE bank_accounts SET balance = balance - 50000 WHERE holder = 'Alice';
    -- oops, Alice doesn't have 50000! Check constraint will fail or we catch it
ROLLBACK;  -- undo all changes

SELECT * FROM bank_accounts;  -- Alice balance unchanged

-- ============================================
-- SAVEPOINT — partial rollback
-- ============================================

BEGIN;
    UPDATE bank_accounts SET balance = balance - 500 WHERE holder = 'Alice';
    INSERT INTO audit_log (action, details) VALUES ('debit', 'Alice: -500');

    SAVEPOINT sp1;

    UPDATE bank_accounts SET balance = balance - 100000 WHERE holder = 'Bob';
    -- this would fail or be wrong, so rollback to savepoint

    ROLLBACK TO SAVEPOINT sp1;
    -- Bob's update is undone, but Alice's debit and audit log remain

    UPDATE bank_accounts SET balance = balance + 500 WHERE holder = 'Charlie';
    INSERT INTO audit_log (action, details) VALUES ('credit', 'Charlie: +500');
COMMIT;

SELECT * FROM bank_accounts;

-- ============================================
-- Autocommit behavior
-- ============================================

-- without BEGIN, each statement is its own transaction
UPDATE bank_accounts SET balance = balance + 100 WHERE holder = 'Alice';
-- this is auto-committed immediately

-- ============================================
-- Transaction Isolation Levels
-- ============================================

-- set isolation level (must be first statement after BEGIN)
BEGIN TRANSACTION ISOLATION LEVEL READ COMMITTED;
    SELECT * FROM bank_accounts;
COMMIT;

BEGIN TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    SELECT * FROM bank_accounts;
    -- other transactions can modify data, but this transaction
    -- will see a consistent snapshot from when it started
COMMIT;

BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    SELECT * FROM bank_accounts;
    -- strictest level: transactions behave as if run one-at-a-time
COMMIT;

-- check current isolation level
SHOW transaction_isolation;

-- ============================================
-- Row-level locking: FOR UPDATE
-- ============================================

-- lock Alice's row so no one else can modify it until we commit
BEGIN;
    SELECT * FROM bank_accounts WHERE holder = 'Alice' FOR UPDATE;
    -- Alice's row is now locked for this transaction
    -- other transactions trying to UPDATE Alice will WAIT

    UPDATE bank_accounts SET balance = balance - 100 WHERE holder = 'Alice';
COMMIT;

-- FOR UPDATE NOWAIT — fail immediately if row is locked
-- FOR UPDATE SKIP LOCKED — skip locked rows (useful for job queues)

-- ============================================
-- Job queue pattern with SKIP LOCKED
-- ============================================

DROP TABLE IF EXISTS job_queue;
CREATE TABLE job_queue (
    id SERIAL PRIMARY KEY,
    payload TEXT,
    status TEXT DEFAULT 'pending',
    worker TEXT
);

INSERT INTO job_queue (payload) VALUES ('job1'), ('job2'), ('job3'), ('job4'), ('job5');

-- worker picks up next available job (skips already-locked ones)
BEGIN;
    UPDATE job_queue
    SET status = 'processing', worker = 'worker_1'
    WHERE id = (
        SELECT id FROM job_queue
        WHERE status = 'pending'
        ORDER BY id
        LIMIT 1
        FOR UPDATE SKIP LOCKED
    )
    RETURNING *;
COMMIT;

SELECT * FROM job_queue;

-- ============================================
-- Advisory Locks (application-level locks)
-- ============================================

-- get an advisory lock (returns true if acquired)
SELECT pg_try_advisory_lock(12345);

-- do work...
-- other sessions calling pg_try_advisory_lock(12345) will get false

-- release the lock
SELECT pg_advisory_unlock(12345);

-- ============================================
-- Checking for locks
-- ============================================

-- view current locks
SELECT pid, locktype, relation::regclass, mode, granted
FROM pg_locks
WHERE relation IS NOT NULL;

-- view blocked queries
SELECT
    blocked.pid AS blocked_pid,
    blocked.query AS blocked_query,
    blocking.pid AS blocking_pid,
    blocking.query AS blocking_query
FROM pg_stat_activity blocked
JOIN pg_locks bl ON blocked.pid = bl.pid AND NOT bl.granted
JOIN pg_locks kl ON bl.locktype = kl.locktype
    AND bl.relation = kl.relation AND kl.granted
JOIN pg_stat_activity blocking ON kl.pid = blocking.pid
WHERE blocked.pid != blocking.pid;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Write a transaction that transfers money between two accounts
--     and logs the transfer in audit_log. If balance goes negative, rollback.

-- Q2: Using SAVEPOINT, write a transaction that:
--     - debits Alice 500
--     - tries to debit Bob 100000 (fails)
--     - rolls back only Bob's part
--     - credits Charlie 500
--     - commits

-- Q3: Explain the difference between READ COMMITTED and REPEATABLE READ
--     (write as comments)

-- Q4: Create a simple job queue and show how two workers would pick
--     different jobs using SKIP LOCKED

-- Q5: What happens when two transactions try to update the same row?
--     (explain as comments — this is a deadlock scenario)

-- ============================================
-- 14b - Stored Procedures Practice (Postgres 11+)
-- ============================================

-- Setup
DROP TABLE IF EXISTS accounts_sp, transfer_log, pending_jobs CASCADE;

CREATE TABLE accounts_sp (
    id SERIAL PRIMARY KEY,
    holder TEXT NOT NULL UNIQUE,
    balance NUMERIC(12,2) NOT NULL CHECK (balance >= 0)
);

CREATE TABLE transfer_log (
    id SERIAL PRIMARY KEY,
    from_acct TEXT,
    to_acct TEXT,
    amount NUMERIC(12,2),
    status TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE pending_jobs (
    id SERIAL PRIMARY KEY,
    payload TEXT,
    status TEXT DEFAULT 'pending',
    processed_at TIMESTAMPTZ
);

INSERT INTO accounts_sp (holder, balance) VALUES
('Alice', 10000), ('Bob', 5000), ('Charlie', 3000);

INSERT INTO pending_jobs (payload) SELECT 'job_' || i FROM generate_series(1, 20) i;

-- ============================================
-- 1. Basic Procedure — No Return
-- ============================================

CREATE OR REPLACE PROCEDURE log_transfer(
    p_from TEXT, p_to TEXT, p_amount NUMERIC, p_status TEXT
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO transfer_log (from_acct, to_acct, amount, status)
    VALUES (p_from, p_to, p_amount, p_status);
END;
$$;

CALL log_transfer('Alice', 'Bob', 1000, 'SUCCESS');
SELECT * FROM transfer_log;

-- ============================================
-- 2. Procedure With Business Logic
-- ============================================

CREATE OR REPLACE PROCEDURE transfer_money(
    p_from TEXT, p_to TEXT, p_amount NUMERIC
)
LANGUAGE plpgsql AS $$
DECLARE
    from_balance NUMERIC;
BEGIN
    -- check balance
    SELECT balance INTO from_balance
    FROM accounts_sp WHERE holder = p_from;

    IF from_balance IS NULL THEN
        RAISE EXCEPTION 'Account % not found', p_from;
    END IF;

    IF from_balance < p_amount THEN
        -- log failed attempt
        INSERT INTO transfer_log (from_acct, to_acct, amount, status)
        VALUES (p_from, p_to, p_amount, 'FAILED: insufficient funds');
        RAISE EXCEPTION 'Insufficient funds: % has only %', p_from, from_balance;
    END IF;

    -- do the transfer
    UPDATE accounts_sp SET balance = balance - p_amount WHERE holder = p_from;
    UPDATE accounts_sp SET balance = balance + p_amount WHERE holder = p_to;

    -- log success
    INSERT INTO transfer_log (from_acct, to_acct, amount, status)
    VALUES (p_from, p_to, p_amount, 'SUCCESS');

    RAISE NOTICE 'Transferred % from % to %', p_amount, p_from, p_to;
END;
$$;

-- successful transfer
CALL transfer_money('Alice', 'Bob', 2000);
SELECT * FROM accounts_sp;
SELECT * FROM transfer_log;

-- failed transfer (uncomment to test)
-- CALL transfer_money('Charlie', 'Bob', 50000);

-- ============================================
-- 3. Procedure With OUT Parameters
-- ============================================

CREATE OR REPLACE PROCEDURE check_balance(
    IN p_holder TEXT,
    OUT p_balance NUMERIC,
    OUT p_status TEXT
)
LANGUAGE plpgsql AS $$
BEGIN
    SELECT balance INTO p_balance
    FROM accounts_sp WHERE holder = p_holder;

    IF p_balance IS NULL THEN
        p_status := 'Account not found';
        p_balance := 0;
    ELSIF p_balance < 1000 THEN
        p_status := 'Low balance warning';
    ELSE
        p_status := 'OK';
    END IF;
END;
$$;

CALL check_balance('Alice', NULL, NULL);
CALL check_balance('Charlie', NULL, NULL);
CALL check_balance('Nobody', NULL, NULL);

-- ============================================
-- 4. Procedure With Transaction Control
--    (THE key advantage over functions)
-- ============================================

CREATE OR REPLACE PROCEDURE batch_process_jobs(batch_size INT DEFAULT 5)
LANGUAGE plpgsql AS $$
DECLARE
    rec RECORD;
    counter INT := 0;
BEGIN
    FOR rec IN
        SELECT id, payload FROM pending_jobs
        WHERE status = 'pending'
        ORDER BY id
    LOOP
        -- process the job
        UPDATE pending_jobs
        SET status = 'done', processed_at = NOW()
        WHERE id = rec.id;

        counter := counter + 1;

        -- commit every batch_size rows
        -- THIS IS ONLY POSSIBLE IN A PROCEDURE!
        IF counter % batch_size = 0 THEN
            COMMIT;
            RAISE NOTICE 'Committed batch: % jobs processed so far', counter;
        END IF;
    END LOOP;

    -- final commit for remaining rows
    COMMIT;
    RAISE NOTICE 'Done! Total jobs processed: %', counter;
END;
$$;

CALL batch_process_jobs(5);
SELECT * FROM pending_jobs;

-- ============================================
-- 5. Procedure With Rollback Control
-- ============================================

-- reset jobs for demo
UPDATE pending_jobs SET status = 'pending', processed_at = NULL;

CREATE OR REPLACE PROCEDURE safe_batch_process()
LANGUAGE plpgsql AS $$
DECLARE
    rec RECORD;
    counter INT := 0;
BEGIN
    FOR rec IN
        SELECT id, payload FROM pending_jobs
        WHERE status = 'pending' ORDER BY id
    LOOP
        BEGIN
            -- try to process
            UPDATE pending_jobs
            SET status = 'done', processed_at = NOW()
            WHERE id = rec.id;

            counter := counter + 1;

            -- simulate error on job 10
            IF rec.id = 10 THEN
                RAISE EXCEPTION 'Simulated error on job %', rec.id;
            END IF;

        EXCEPTION WHEN OTHERS THEN
            -- rollback just this iteration, continue with others
            RAISE NOTICE 'Error on job %: %. Skipping.', rec.id, SQLERRM;
            ROLLBACK;
        END;

        -- commit successful jobs
        IF counter % 5 = 0 THEN
            COMMIT;
        END IF;
    END LOOP;

    COMMIT;
    RAISE NOTICE 'Batch complete: % jobs processed', counter;
END;
$$;

CALL safe_batch_process();
SELECT * FROM pending_jobs ORDER BY id;

-- ============================================
-- 6. Comparison: Function vs Procedure
-- ============================================

-- FUNCTION: can use in SELECT, cannot COMMIT inside
CREATE OR REPLACE FUNCTION get_balance(p_holder TEXT)
RETURNS NUMERIC AS $$
    SELECT balance FROM accounts_sp WHERE holder = p_holder;
$$ LANGUAGE SQL STABLE;

-- use in SELECT — works!
SELECT holder, get_balance(holder) FROM accounts_sp;
SELECT * FROM accounts_sp WHERE get_balance(holder) > 5000;

-- PROCEDURE: use with CALL, CAN commit inside
-- CALL transfer_money('Alice', 'Bob', 500);

-- SELECT transfer_money('Alice', 'Bob', 500);  -- ERROR! procedures can't be in SELECT

-- ============================================
-- 7. Drop Procedures
-- ============================================

-- must specify param types (Postgres uses them to identify overloads)
-- DROP PROCEDURE transfer_money(TEXT, TEXT, NUMERIC);
-- DROP PROCEDURE IF EXISTS transfer_money(TEXT, TEXT, NUMERIC);

-- list all procedures
SELECT routine_name, routine_type
FROM information_schema.routines
WHERE routine_schema = 'public' AND routine_type = 'PROCEDURE';

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Create a procedure `add_account(holder TEXT, initial_balance NUMERIC)`
--     that inserts into accounts_sp and logs the action

-- Q2: Create a procedure `close_account(holder TEXT)` that:
--     - checks if balance is 0
--     - if not, raises exception
--     - if yes, deletes the account and logs it

-- Q3: Create a procedure with OUT params that returns
--     total accounts, total balance, and average balance

-- Q4: Create a batch procedure that processes pending_jobs
--     and commits every 3 rows. Print progress with RAISE NOTICE.

-- Q5: Why can't you use `CALL transfer_money(...)` inside a SELECT?
--     Why can't you use `COMMIT` inside a function?
--     (answer as SQL comments)

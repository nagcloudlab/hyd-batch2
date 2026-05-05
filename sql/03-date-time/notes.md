# 03 - Date & Time (Postgres)

---

## Current Date/Time

```sql
SELECT NOW();              -- 2024-06-15 10:30:45.123+05:30
SELECT CURRENT_DATE;       -- 2024-06-15
SELECT CURRENT_TIME;       -- 10:30:45.123+05:30
SELECT CURRENT_TIMESTAMP;  -- same as NOW()
```

---

## Date/Time Types

| Type | Example | Use When |
|------|---------|----------|
| `DATE` | `2024-06-15` | date only (birthdays, deadlines) |
| `TIME` | `10:30:00` | time only (schedules) |
| `TIMESTAMP` | `2024-06-15 10:30:00` | date+time, no timezone |
| `TIMESTAMPTZ` | `2024-06-15 10:30:00+05:30` | date+time+timezone (RECOMMENDED) |
| `INTERVAL` | `2 days 3 hours` | duration |

> Always use TIMESTAMPTZ for real applications. TIMESTAMP without timezone causes bugs across servers.

---

## AGE() — Difference Between Dates

```sql
-- how old is this date?
SELECT AGE('2020-01-15');
-- 4 years 5 months 0 days

-- difference between two dates
SELECT AGE('2024-06-15', '2024-01-01');
-- 5 mons 14 days

-- employee tenure
SELECT name, AGE(NOW(), hire_date) AS tenure FROM employees;
```

---

## EXTRACT() — Pull Parts From a Date

```sql
SELECT
    EXTRACT(YEAR   FROM TIMESTAMP '2024-06-15 10:30:00') AS year,    -- 2024
    EXTRACT(MONTH  FROM TIMESTAMP '2024-06-15 10:30:00') AS month,   -- 6
    EXTRACT(DAY    FROM TIMESTAMP '2024-06-15 10:30:00') AS day,     -- 15
    EXTRACT(HOUR   FROM TIMESTAMP '2024-06-15 10:30:00') AS hour,    -- 10
    EXTRACT(DOW    FROM TIMESTAMP '2024-06-15 10:30:00') AS dow,     -- 6 (Sat, 0=Sun)
    EXTRACT(QUARTER FROM TIMESTAMP '2024-06-15 10:30:00') AS qtr,   -- 2
    EXTRACT(WEEK   FROM TIMESTAMP '2024-06-15 10:30:00') AS week;    -- 24
```

> DOW: 0=Sunday, 6=Saturday. Use ISODOW for 1=Monday, 7=Sunday.

---

## DATE_TRUNC() — Truncate to Precision

The hero function for time-based reports.

```sql
SELECT DATE_TRUNC('month',  TIMESTAMP '2024-06-15 10:30:00');  -- 2024-06-01 00:00:00
SELECT DATE_TRUNC('year',   TIMESTAMP '2024-06-15 10:30:00');  -- 2024-01-01 00:00:00
SELECT DATE_TRUNC('week',   TIMESTAMP '2024-06-15 10:30:00');  -- 2024-06-10 00:00:00
SELECT DATE_TRUNC('quarter',TIMESTAMP '2024-06-15 10:30:00');  -- 2024-04-01 00:00:00
SELECT DATE_TRUNC('hour',   TIMESTAMP '2024-06-15 10:30:45');  -- 2024-06-15 10:00:00

-- REAL USE: monthly revenue report
SELECT
    DATE_TRUNC('month', order_date) AS month,
    SUM(amount) AS revenue
FROM orders
GROUP BY DATE_TRUNC('month', order_date)
ORDER BY month;
```

---

## TO_CHAR() — Format Date as String

```sql
SELECT TO_CHAR(NOW(), 'DD-Mon-YYYY');          -- 15-Jun-2024
SELECT TO_CHAR(NOW(), 'YYYY/MM/DD');           -- 2024/06/15
SELECT TO_CHAR(NOW(), 'Day, DD Month YYYY');   -- Saturday, 15 June 2024
SELECT TO_CHAR(NOW(), 'HH12:MI AM');           -- 10:30 AM
SELECT TO_CHAR(NOW(), 'HH24:MI:SS');           -- 10:30:45
```

| Pattern | Meaning | Example |
|---------|---------|---------|
| `YYYY` | 4-digit year | 2024 |
| `MM` | month number | 06 |
| `Mon` | short month | Jun |
| `Month` | full month | June |
| `DD` | day of month | 15 |
| `Day` | full day name | Saturday |
| `HH24` | 24-hour | 14 |
| `HH12` | 12-hour | 02 |
| `MI` | minutes | 30 |
| `SS` | seconds | 45 |
| `AM` | AM/PM | PM |

---

## TO_DATE() / TO_TIMESTAMP() — Parse Strings

```sql
SELECT TO_DATE('15-01-2024', 'DD-MM-YYYY');               -- 2024-01-15
SELECT TO_TIMESTAMP('2024/06/15 14:30', 'YYYY/MM/DD HH24:MI');  -- timestamp
```

---

## INTERVAL — Duration Arithmetic

```sql
-- add time
SELECT NOW() + INTERVAL '30 days';
SELECT NOW() + INTERVAL '2 hours 30 minutes';
SELECT NOW() + INTERVAL '1 year 3 months';

-- subtract time
SELECT NOW() - INTERVAL '1 week';

-- orders from last 7 days
SELECT * FROM orders WHERE order_date >= NOW() - INTERVAL '7 days';

-- subscription expiry
SELECT name, signup_date + INTERVAL '1 year' AS expires_on FROM users;

-- simple date math
SELECT '2024-12-31'::DATE - '2024-01-01'::DATE AS days_diff;  -- 365
```

---

## AT TIME ZONE — Timezone Conversion

```sql
-- convert to IST
SELECT NOW() AT TIME ZONE 'Asia/Kolkata' AS ist;

-- compare zones
SELECT
    NOW() AT TIME ZONE 'UTC'          AS utc,
    NOW() AT TIME ZONE 'Asia/Kolkata' AS ist,
    NOW() AT TIME ZONE 'US/Eastern'   AS est;
```

---

## generate_series() — Date Ranges

```sql
-- all dates in January 2024
SELECT d::DATE FROM generate_series('2024-01-01', '2024-01-31', INTERVAL '1 day') d;

-- all months of 2024
SELECT d::DATE FROM generate_series('2024-01-01', '2024-12-01', INTERVAL '1 month') d;

-- REAL USE: find days with no orders
SELECT d::DATE AS date
FROM generate_series('2024-01-01', '2024-12-31', INTERVAL '1 day') d
LEFT JOIN orders o ON d::DATE = o.order_date::DATE
WHERE o.id IS NULL;
```

> generate_series is incredibly useful for filling gaps in reports — months with 0 sales, days with no activity, etc.

---

## Quick Real-World Examples

```sql
-- monthly report with month name
SELECT
    TO_CHAR(DATE_TRUNC('month', order_date), 'Month YYYY') AS month,
    COUNT(*) AS orders,
    SUM(amount) AS revenue
FROM orders GROUP BY 1 ORDER BY DATE_TRUNC('month', order_date);

-- is it a weekday or weekend?
SELECT order_date,
    CASE WHEN EXTRACT(DOW FROM order_date) IN (0, 6) THEN 'Weekend' ELSE 'Weekday' END
FROM orders;

-- first and last day of current month
SELECT
    DATE_TRUNC('month', NOW())::DATE AS first_day,
    (DATE_TRUNC('month', NOW()) + INTERVAL '1 month - 1 day')::DATE AS last_day;
```

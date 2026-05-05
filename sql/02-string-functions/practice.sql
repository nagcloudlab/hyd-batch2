-- ============================================
-- 02 - String Functions Practice (Postgres)
-- ============================================

DROP TABLE IF EXISTS customers;
CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(200)
);

INSERT INTO customers (first_name, last_name, email, phone, address) VALUES
('john', 'doe', 'JOHN.DOE@gmail.com', '91-9876543210', '123 MG Road, Hyderabad'),
('Jane', 'SMITH', 'jane.smith@Yahoo.COM', '91-8765432109', '456 Brigade Rd, Bangalore'),
('  Bob  ', 'Williams', 'bob.w@outlook.com', '91-7654321098', '789 Anna Salai, Chennai'),
('Alice', 'johnson', 'alice.j@hotmail.com', '91-6543210987', '321 Park Street, Mumbai'),
('Charlie', 'Brown', 'charlie.b@gmail.com', '91-5432109876', '654 MG Road, Pune');

-- ============================================
-- UPPER, LOWER, INITCAP
-- ============================================

-- convert to uppercase
SELECT UPPER(first_name) AS upper_name FROM customers;

-- convert to lowercase
SELECT LOWER(email) AS clean_email FROM customers;

-- capitalize first letter of each word (Postgres specific)
SELECT INITCAP(first_name || ' ' || last_name) AS full_name FROM customers;

-- ============================================
-- LENGTH / CHAR_LENGTH
-- ============================================

SELECT first_name, LENGTH(first_name) AS name_length FROM customers;

-- LENGTH and CHAR_LENGTH are same for ASCII
-- CHAR_LENGTH counts characters, OCTET_LENGTH counts bytes (matters for unicode)

-- ============================================
-- CONCAT / || operator
-- ============================================

-- using CONCAT function
SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM customers;

-- using || operator (Postgres preferred)
SELECT first_name || ' ' || last_name AS full_name FROM customers;

-- CONCAT_WS = concat with separator
SELECT CONCAT_WS(', ', address, 'India') AS full_address FROM customers;

-- ============================================
-- SUBSTRING / LEFT / RIGHT
-- ============================================

-- extract characters: SUBSTRING(string FROM start FOR length)
SELECT SUBSTRING(phone FROM 4 FOR 10) AS phone_number FROM customers;

-- alternate syntax
SELECT SUBSTRING(email, 1, POSITION('@' IN email) - 1) AS username FROM customers;

-- LEFT: first N characters
SELECT LEFT(first_name, 3) AS short_name FROM customers;

-- RIGHT: last N characters
SELECT RIGHT(phone, 4) AS last_four FROM customers;

-- ============================================
-- TRIM / LTRIM / RTRIM
-- ============================================

-- remove leading and trailing spaces
SELECT TRIM(first_name) AS trimmed FROM customers;

-- remove only leading spaces
SELECT LTRIM(first_name) AS left_trimmed FROM customers;

-- remove only trailing spaces
SELECT RTRIM(first_name) AS right_trimmed FROM customers;

-- trim specific characters
SELECT TRIM(BOTH '.' FROM '...hello...') AS result;  -- hello

-- ============================================
-- REPLACE
-- ============================================

-- replace substring
SELECT REPLACE(phone, '-', ' ') AS formatted_phone FROM customers;

-- remove all dashes
SELECT REPLACE(phone, '-', '') AS clean_phone FROM customers;

-- ============================================
-- POSITION / STRPOS
-- ============================================

-- find position of '@' in email (1-based index)
SELECT email, POSITION('@' IN email) AS at_position FROM customers;

-- STRPOS is Postgres equivalent
SELECT email, STRPOS(email, '@') AS at_position FROM customers;

-- ============================================
-- SPLIT_PART (Postgres specific)
-- ============================================

-- split email at '@' and get domain
SELECT email, SPLIT_PART(email, '@', 2) AS domain FROM customers;

-- split address at ',' and get city
SELECT address, TRIM(SPLIT_PART(address, ',', 2)) AS city FROM customers;

-- ============================================
-- REPEAT / REVERSE
-- ============================================

SELECT REPEAT('*', 5) AS stars;           -- *****
SELECT REVERSE('hello') AS reversed;       -- olleh

-- mask email: show first 2 chars + ****
SELECT LEFT(email, 2) || REPEAT('*', 6) || '@' || SPLIT_PART(email, '@', 2) AS masked
FROM customers;

-- ============================================
-- LPAD / RPAD
-- ============================================

-- pad employee id to 5 digits with zeros
SELECT LPAD(id::TEXT, 5, '0') AS emp_id FROM customers;

-- right pad name to 15 chars
SELECT RPAD(first_name, 15, '.') AS padded_name FROM customers;

-- ============================================
-- REGEXP_MATCHES / REGEXP_REPLACE
-- ============================================

-- extract digits from phone
SELECT REGEXP_REPLACE(phone, '[^0-9]', '', 'g') AS digits_only FROM customers;

-- check if email is gmail
SELECT email, email ~ 'gmail\.com$' AS is_gmail FROM customers;

-- replace multiple spaces with single space
SELECT REGEXP_REPLACE('hello    world', '\s+', ' ', 'g') AS cleaned;

-- extract domain name without extension
SELECT REGEXP_REPLACE(SPLIT_PART(email, '@', 2), '\.\w+$', '') AS domain_name
FROM customers;

-- ============================================
-- FORMAT (Postgres specific)
-- ============================================

-- printf-style formatting
SELECT FORMAT('Hello, %s %s!', first_name, last_name) AS greeting FROM customers;

-- %I for identifiers, %L for literals (safe from SQL injection)
SELECT FORMAT('Name: %s, Email: %s', INITCAP(first_name), LOWER(email)) AS info
FROM customers;

-- ============================================
-- PRACTICAL: Clean up messy data
-- ============================================

-- clean all customer data in one query
SELECT
    id,
    INITCAP(TRIM(first_name)) AS first_name,
    INITCAP(TRIM(last_name)) AS last_name,
    LOWER(TRIM(email)) AS email,
    REGEXP_REPLACE(phone, '[^0-9]', '', 'g') AS phone,
    TRIM(address) AS address
FROM customers;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Extract the street number from the address column

-- Q2: Create a username from first letter of first_name + last_name (lowercase)
--     e.g., john doe -> jdoe

-- Q3: Mask phone number: show only last 4 digits, rest as *
--     e.g., 9876543210 -> ******3210

-- Q4: Find all customers whose email domain is NOT gmail.com

-- Q5: Reverse each customer's full name and display in uppercase

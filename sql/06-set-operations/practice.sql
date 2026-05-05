-- ============================================
-- 06 - Set Operations Practice
-- ============================================

DROP TABLE IF EXISTS students_2023, students_2024, teachers;

CREATE TABLE students_2023 (name TEXT, course TEXT, city TEXT);
CREATE TABLE students_2024 (name TEXT, course TEXT, city TEXT);
CREATE TABLE teachers (name TEXT, subject TEXT, city TEXT);

INSERT INTO students_2023 VALUES
('Alice', 'SQL', 'Hyderabad'), ('Bob', 'Python', 'Mumbai'),
('Charlie', 'SQL', 'Bangalore'), ('Diana', 'Java', 'Chennai'),
('Eve', 'Python', 'Hyderabad');

INSERT INTO students_2024 VALUES
('Alice', 'SQL', 'Hyderabad'), ('Frank', 'SQL', 'Pune'),
('Charlie', 'React', 'Bangalore'), ('Grace', 'Python', 'Mumbai'),
('Bob', 'Python', 'Mumbai');

INSERT INTO teachers VALUES
('Mr. Rao', 'SQL', 'Hyderabad'), ('Ms. Sharma', 'Python', 'Mumbai'),
('Mr. Patel', 'Java', 'Bangalore');

-- ============================================
-- UNION (removes duplicates)
-- ============================================

-- all unique students across both years
SELECT name, city FROM students_2023
UNION
SELECT name, city FROM students_2024
ORDER BY name;

-- ============================================
-- UNION ALL (keeps duplicates — faster)
-- ============================================

-- all student records including duplicates
SELECT name, course, '2023' AS year FROM students_2023
UNION ALL
SELECT name, course, '2024' AS year FROM students_2024
ORDER BY name;

-- ============================================
-- INTERSECT (rows in BOTH queries)
-- ============================================

-- students enrolled in both years (same name + city)
SELECT name, city FROM students_2023
INTERSECT
SELECT name, city FROM students_2024;

-- ============================================
-- EXCEPT (rows in first but NOT in second)
-- ============================================

-- students who were in 2023 but NOT in 2024
SELECT name, city FROM students_2023
EXCEPT
SELECT name, city FROM students_2024;

-- students who are in 2024 but NOT in 2023
SELECT name, city FROM students_2024
EXCEPT
SELECT name, city FROM students_2023;

-- ============================================
-- PRACTICAL: combine different tables
-- ============================================

-- all people (students + teachers) with their role
SELECT name, city, 'Student' AS role FROM students_2024
UNION
SELECT name, city, 'Teacher' AS role FROM teachers
ORDER BY role, name;

-- cities that have both students and teachers
SELECT city FROM students_2024
INTERSECT
SELECT city FROM teachers;

-- cities with students but no teachers
SELECT DISTINCT city FROM students_2024
EXCEPT
SELECT city FROM teachers;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Find courses offered in 2023 but not in 2024

-- Q2: List all unique courses across both years

-- Q3: Find students who changed their course between 2023 and 2024
--     (present in both years but different course)

-- Q4: Count how many total enrollments (including duplicates) across both years

-- Q5: Find cities where we have teachers but no students in 2024

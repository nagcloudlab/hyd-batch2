# 06 - Set Operations

---

## UNION — Combine Results, Remove Duplicates

```sql
-- all students from both years (no duplicates)
SELECT name, city FROM students_2023
UNION
SELECT name, city FROM students_2024;
```

---

## UNION ALL — Combine Results, Keep Duplicates (Faster)

```sql
-- all records including duplicates, with a label
SELECT name, course, '2023' AS year FROM students_2023
UNION ALL
SELECT name, course, '2024' AS year FROM students_2024;
```

> UNION ALL is faster because it skips the dedup step. Use it when duplicates are OK or impossible.

---

## INTERSECT — Only Rows in BOTH Queries

```sql
-- students enrolled in both years
SELECT name, city FROM students_2023
INTERSECT
SELECT name, city FROM students_2024;
```

```
 name  | city
-------+-----------
 Alice | Hyderabad    -- present in both years
 Bob   | Mumbai       -- present in both years
```

---

## EXCEPT — Rows in First but NOT in Second

```sql
-- students in 2023 who did NOT return in 2024
SELECT name, city FROM students_2023
EXCEPT
SELECT name, city FROM students_2024;

-- students NEW in 2024 (not in 2023)
SELECT name, city FROM students_2024
EXCEPT
SELECT name, city FROM students_2023;
```

> Order matters! `A EXCEPT B` is different from `B EXCEPT A`.

---

## Practical: Combine Different Tables

```sql
-- all people with their role
SELECT name, city, 'Student' AS role FROM students
UNION
SELECT name, city, 'Teacher' AS role FROM teachers
ORDER BY role, name;
```

---

## Rules

| Rule | Detail |
|------|--------|
| Same column count | all queries must return same number of columns |
| Compatible types | column data types must match or be castable |
| Column names | taken from the FIRST query |
| ORDER BY | goes at the very END, applies to combined result |
| Duplicates | UNION/INTERSECT/EXCEPT remove dupes. Add ALL to keep them. |

---

## Quick Comparison

```
A = {1, 2, 3, 4}
B = {3, 4, 5, 6}

A UNION B     = {1, 2, 3, 4, 5, 6}
A UNION ALL B = {1, 2, 3, 4, 3, 4, 5, 6}
A INTERSECT B = {3, 4}
A EXCEPT B    = {1, 2}
B EXCEPT A    = {5, 6}
```

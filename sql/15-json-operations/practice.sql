-- ============================================
-- 15 - JSON / JSONB Operations Practice
-- ============================================

DROP TABLE IF EXISTS user_profiles;
CREATE TABLE user_profiles (
    id SERIAL PRIMARY KEY,
    name TEXT,
    profile JSONB
);

INSERT INTO user_profiles (name, profile) VALUES
('Alice', '{"age": 28, "city": "Hyderabad", "skills": ["SQL", "Python"], "address": {"street": "MG Road", "pin": "500001"}, "active": true}'),
('Bob', '{"age": 32, "city": "Mumbai", "skills": ["Java", "React", "Node"], "address": {"street": "Marine Drive", "pin": "400001"}, "active": true}'),
('Charlie', '{"age": 25, "city": "Bangalore", "skills": ["Python"], "address": {"street": "MG Road", "pin": "560001"}, "active": false}'),
('Diana', '{"age": 35, "city": "Chennai", "skills": ["SQL", "Java", "Python", "Go"], "address": {"street": "Anna Salai", "pin": "600001"}, "active": true}');

-- ============================================
-- -> (get as JSON) vs ->> (get as TEXT)
-- ============================================

-- -> returns JSON type
SELECT name, profile -> 'city' AS city_json FROM user_profiles;

-- ->> returns TEXT type (usually what you want)
SELECT name, profile ->> 'city' AS city_text FROM user_profiles;

-- get age as integer (cast from text)
SELECT name, (profile ->> 'age')::INT AS age FROM user_profiles;

-- ============================================
-- Nested access with -> and ->>
-- ============================================

-- access nested object
SELECT name, profile -> 'address' ->> 'street' AS street FROM user_profiles;
SELECT name, profile -> 'address' ->> 'pin' AS pin FROM user_profiles;

-- #> and #>> for path-based access
SELECT name, profile #>> '{address, street}' AS street FROM user_profiles;
SELECT name, profile #> '{skills, 0}' AS first_skill FROM user_profiles;

-- ============================================
-- @> containment (does JSON contain this?)
-- ============================================

-- find users in Hyderabad
SELECT * FROM user_profiles WHERE profile @> '{"city": "Hyderabad"}';

-- find active users
SELECT name FROM user_profiles WHERE profile @> '{"active": true}';

-- find users with specific skill in array
SELECT name FROM user_profiles WHERE profile @> '{"skills": ["Python"]}';

-- ============================================
-- ? key existence
-- ============================================

-- check if key exists
SELECT name FROM user_profiles WHERE profile ? 'age';

-- ?| any of these keys exist
SELECT name FROM user_profiles WHERE profile ?| ARRAY['age', 'salary'];

-- ?& all of these keys exist
SELECT name FROM user_profiles WHERE profile ?& ARRAY['age', 'city', 'active'];

-- ============================================
-- jsonb_array_elements — expand array to rows
-- ============================================

-- list all skills per user (one row per skill)
SELECT name, skill
FROM user_profiles,
     jsonb_array_elements_text(profile -> 'skills') AS skill;

-- count skills per user
SELECT name, jsonb_array_length(profile -> 'skills') AS skill_count
FROM user_profiles;

-- find users who know SQL
SELECT DISTINCT name
FROM user_profiles,
     jsonb_array_elements_text(profile -> 'skills') AS skill
WHERE skill = 'SQL';

-- ============================================
-- jsonb_each — expand object to key-value rows
-- ============================================

SELECT name, key, value
FROM user_profiles,
     jsonb_each(profile) AS kv(key, value)
WHERE name = 'Alice';

-- text version
SELECT name, key, value
FROM user_profiles,
     jsonb_each_text(profile) AS kv(key, value)
WHERE name = 'Alice';

-- ============================================
-- jsonb_object_keys — get all keys
-- ============================================

SELECT DISTINCT jsonb_object_keys(profile) AS key
FROM user_profiles
ORDER BY key;

-- ============================================
-- MODIFYING JSONB
-- ============================================

-- jsonb_set: update a value
UPDATE user_profiles
SET profile = jsonb_set(profile, '{city}', '"Pune"')
WHERE name = 'Charlie';

-- jsonb_set: add a new key
UPDATE user_profiles
SET profile = jsonb_set(profile, '{email}', '"alice@example.com"')
WHERE name = 'Alice';

-- || merge (add/overwrite keys)
UPDATE user_profiles
SET profile = profile || '{"phone": "9876543210", "verified": true}'
WHERE name = 'Bob';

-- - delete a key
UPDATE user_profiles
SET profile = profile - 'active'
WHERE name = 'Charlie';

-- delete from array by index
-- remove first skill (index 0)
-- UPDATE user_profiles
-- SET profile = profile #- '{skills, 0}'
-- WHERE name = 'Alice';

SELECT name, profile FROM user_profiles;

-- ============================================
-- jsonb_build_object / jsonb_build_array
-- ============================================

-- construct JSON from columns
SELECT jsonb_build_object(
    'id', id,
    'name', name,
    'city', profile ->> 'city'
) AS user_json
FROM user_profiles;

-- construct JSON array
SELECT jsonb_build_array(1, 'hello', true, null) AS arr;

-- ============================================
-- jsonb_agg / json_agg — aggregate rows into JSON
-- ============================================

-- all user names as JSON array
SELECT jsonb_agg(name) AS all_names FROM user_profiles;

-- group skills by city
SELECT
    profile ->> 'city' AS city,
    jsonb_agg(DISTINCT skill) AS all_skills
FROM user_profiles,
     jsonb_array_elements_text(profile -> 'skills') AS skill
GROUP BY profile ->> 'city';

-- entire table as JSON array
SELECT jsonb_agg(jsonb_build_object('name', name, 'city', profile ->> 'city'))
FROM user_profiles;

-- ============================================
-- jsonb_to_record — convert JSON to row
-- ============================================

SELECT name, r.*
FROM user_profiles,
     jsonb_to_record(profile) AS r(age INT, city TEXT, active BOOLEAN);

-- ============================================
-- GIN Index on JSONB
-- ============================================

CREATE INDEX idx_profile_gin ON user_profiles USING GIN(profile);

-- these queries benefit from GIN index:
EXPLAIN ANALYZE SELECT * FROM user_profiles WHERE profile @> '{"city": "Mumbai"}';
EXPLAIN ANALYZE SELECT * FROM user_profiles WHERE profile ? 'email';

-- index on specific path
CREATE INDEX idx_profile_city ON user_profiles((profile ->> 'city'));
EXPLAIN ANALYZE SELECT * FROM user_profiles WHERE profile ->> 'city' = 'Mumbai';

-- ============================================
-- PRACTICAL: API-style query
-- ============================================

-- build a JSON response for an API
SELECT jsonb_build_object(
    'total_users', (SELECT COUNT(*) FROM user_profiles),
    'users', (
        SELECT jsonb_agg(
            jsonb_build_object(
                'name', name,
                'city', profile ->> 'city',
                'skills', profile -> 'skills',
                'skill_count', jsonb_array_length(profile -> 'skills')
            )
        )
        FROM user_profiles
    )
) AS api_response;

-- ============================================
-- EXERCISES
-- ============================================

-- Q1: Find users who have more than 2 skills

-- Q2: Add a "rating" key with value 4.5 to all users who are active

-- Q3: Find the most common skill across all users

-- Q4: Create a query that returns each user's profile as a flat table
--     (name, age, city, street, pin, skill_count)

-- Q5: Build a JSON summary: for each city, count of users and list of names

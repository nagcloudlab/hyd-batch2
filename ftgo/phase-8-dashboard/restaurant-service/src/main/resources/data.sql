-- ==========================================
-- Restaurant Service - Seed Data
-- ==========================================

-- Restaurants
INSERT INTO restaurant (name, address, phone, active) VALUES ('Mumbai Masala Kitchen', 'Juhu Beach Road, Mumbai', '9112233001', true);
INSERT INTO restaurant (name, address, phone, active) VALUES ('Pune Biryani House', 'FC Road, Pune', '9112233002', true);
INSERT INTO restaurant (name, address, phone, active) VALUES ('Delhi Darbar', 'Colaba, Mumbai', '9112233003', true);

-- Menu Items for Mumbai Masala Kitchen (restaurant_id = 1)
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Butter Chicken', 320.00, true, 1);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Paneer Tikka Masala', 280.00, true, 1);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Naan', 40.00, true, 1);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Dal Makhani', 220.00, true, 1);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Gulab Jamun', 80.00, true, 1);

-- Menu Items for Pune Biryani House (restaurant_id = 2)
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Chicken Biryani', 350.00, true, 2);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Mutton Biryani', 450.00, true, 2);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Veg Biryani', 250.00, true, 2);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Raita', 60.00, true, 2);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Phirni', 90.00, true, 2);

-- Menu Items for Delhi Darbar (restaurant_id = 3)
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Tandoori Chicken', 380.00, true, 3);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Chole Bhature', 180.00, true, 3);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Rogan Josh', 420.00, true, 3);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Garlic Naan', 50.00, true, 3);
INSERT INTO menu_item (name, price, available, restaurant_id) VALUES ('Kulfi', 70.00, true, 3);

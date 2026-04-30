-- ==========================================
-- FTGO Monolith (Iteration 2) - Seed Data
-- Restaurant data moved to restaurant-service
-- ==========================================

-- Consumers
INSERT INTO consumer (name, email, phone, address) VALUES ('Rahul Sharma', 'rahul@example.com', '9876543210', '101, Marine Drive, Mumbai');
INSERT INTO consumer (name, email, phone, address) VALUES ('Priya Patel', 'priya@example.com', '9876543211', '202, MG Road, Pune');
INSERT INTO consumer (name, email, phone, address) VALUES ('Amit Kumar', 'amit@example.com', '9876543212', '303, FC Road, Pune');

-- Couriers
INSERT INTO courier (name, phone, available, current_location) VALUES ('Suresh Rider', '9988776601', true, 'Andheri, Mumbai');
INSERT INTO courier (name, phone, available, current_location) VALUES ('Ramesh Delivery', '9988776602', true, 'Bandra, Mumbai');
INSERT INTO courier (name, phone, available, current_location) VALUES ('Vijay Express', '9988776603', true, 'Kothrud, Pune');

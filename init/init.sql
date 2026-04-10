-- Database initialization script
-- This script seeds the database with initial data for development and testing.

-- Remove vector extension if it exists
DROP EXTENSION IF EXISTS vector CASCADE;

-- 1. Create tables if they don't exist (ensures seeding works even before Hibernate runs)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS class_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(500),
    default_capacity INTEGER NOT NULL,
    duration_minutes INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS gym_classes (
    id BIGSERIAL PRIMARY KEY,
    class_type_id BIGINT NOT NULL REFERENCES class_types(id),
    instructor_id BIGINT NOT NULL REFERENCES users(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    capacity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    gym_class_id BIGINT NOT NULL REFERENCES gym_classes(id),
    status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
    booked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    UNIQUE(user_id, gym_class_id)
);

-- 2. Seed Class Types
INSERT INTO class_types (name, description, default_capacity, duration_minutes) VALUES
('Yoga', 'A calming practice focused on flexibility, balance, and mindfulness. Improve your posture, reduce stress, and connect body and mind. Suitable for all levels.', 20, 60),
('HIIT', 'High-intensity interval training designed to maximize calorie burn and boost cardiovascular fitness. Short bursts of explosive exercises.', 25, 45),
('Strength Training', 'Build muscle, increase power, and improve bone density with progressive resistance training using free weights.', 15, 60),
('Spinning', 'An intense indoor cycling workout set to energizing music. Burn calories and improve cardiovascular health.', 30, 45),
('Pilates', 'A low-impact workout emphasizing core strength, flexibility, and body awareness.', 18, 50),
('Boxing Fitness', 'A high-energy workout combining boxing techniques with cardio conditioning.', 20, 60),
('Zumba', 'A fun dance-fitness party with Latin and international music.', 35, 55),
('CrossFit', 'A varied functional fitness program combining weightlifting, gymnastics, and metabolic conditioning.', 16, 60)
ON CONFLICT (name) DO NOTHING;

-- 3. Seed Users (Admins, Instructors, and Members)
INSERT INTO users (email, password_hash, first_name, last_name, phone_number, role) VALUES
('admin@boka.se', '$2a$10$dummyHashedPasswordForSeeding', 'Admin', 'Adminsson', '070-111-1111', 'ADMIN'),
('anna@boka.se', '$2a$10$dummyHashedPasswordForSeeding', 'Anna', 'Johansson', '070-222-2222', 'INSTRUCTOR'),
('erik@boka.se', '$2a$10$dummyHashedPasswordForSeeding', 'Erik', 'Lindberg', '070-333-3333', 'INSTRUCTOR'),
('sara@boka.se', '$2a$10$dummyHashedPasswordForSeeding', 'Sara', 'Nilsson', '070-444-4444', 'INSTRUCTOR'),
('karl@example.com', '$2a$10$dummyHashedPasswordForSeeding', 'Karl', 'Svensson', '070-555-5555', 'MEMBER'),
('lisa@example.com', '$2a$10$dummyHashedPasswordForSeeding', 'Lisa', 'Eriksson', '070-666-6666', 'MEMBER')
ON CONFLICT (email) DO NOTHING;

-- 4. Seed Gym Classes (Dynamic dates relative to CURRENT_DATE)

-- Day 1
INSERT INTO gym_classes (class_type_id, instructor_id, start_time, end_time, capacity)
SELECT ct.id, u.id, CURRENT_DATE + interval '1 day 07:00:00', CURRENT_DATE + interval '1 day 08:00:00', ct.default_capacity
FROM class_types ct, users u WHERE ct.name = 'Yoga' AND u.email = 'anna@boka.se'
ON CONFLICT DO NOTHING;

INSERT INTO gym_classes (class_type_id, instructor_id, start_time, end_time, capacity)
SELECT ct.id, u.id, CURRENT_DATE + interval '1 day 09:00:00', CURRENT_DATE + interval '1 day 09:45:00', ct.default_capacity
FROM class_types ct, users u WHERE ct.name = 'HIIT' AND u.email = 'erik@boka.se'
ON CONFLICT DO NOTHING;

INSERT INTO gym_classes (class_type_id, instructor_id, start_time, end_time, capacity)
SELECT ct.id, u.id, CURRENT_DATE + interval '1 day 12:00:00', CURRENT_DATE + interval '1 day 12:45:00', ct.default_capacity
FROM class_types ct, users u WHERE ct.name = 'Spinning' AND u.email = 'sara@boka.se'
ON CONFLICT DO NOTHING;

INSERT INTO gym_classes (class_type_id, instructor_id, start_time, end_time, capacity)
SELECT ct.id, u.id, CURRENT_DATE + interval '1 day 17:00:00', CURRENT_DATE + interval '1 day 18:00:00', ct.default_capacity
FROM class_types ct, users u WHERE ct.name = 'Strength Training' AND u.email = 'erik@boka.se'
ON CONFLICT DO NOTHING;

-- Day 2
INSERT INTO gym_classes (class_type_id, instructor_id, start_time, end_time, capacity)
SELECT ct.id, u.id, CURRENT_DATE + interval '2 days 08:00:00', CURRENT_DATE + interval '2 days 08:50:00', ct.default_capacity
FROM class_types ct, users u WHERE ct.name = 'Pilates' AND u.email = 'anna@boka.se'
ON CONFLICT DO NOTHING;

INSERT INTO gym_classes (class_type_id, instructor_id, start_time, end_time, capacity)
SELECT ct.id, u.id, CURRENT_DATE + interval '2 days 10:00:00', CURRENT_DATE + interval '2 days 11:00:00', ct.default_capacity
FROM class_types ct, users u WHERE ct.name = 'Boxing Fitness' AND u.email = 'sara@boka.se'
ON CONFLICT DO NOTHING;

INSERT INTO gym_classes (class_type_id, instructor_id, start_time, end_time, capacity)
SELECT ct.id, u.id, CURRENT_DATE + interval '2 days 18:00:00', CURRENT_DATE + interval '2 days 18:55:00', ct.default_capacity
FROM class_types ct, users u WHERE ct.name = 'Zumba' AND u.email = 'anna@boka.se'
ON CONFLICT DO NOTHING;

-- Day 3
INSERT INTO gym_classes (class_type_id, instructor_id, start_time, end_time, capacity)
SELECT ct.id, u.id, CURRENT_DATE + interval '3 days 07:00:00', CURRENT_DATE + interval '3 days 08:00:00', ct.default_capacity
FROM class_types ct, users u WHERE ct.name = 'CrossFit' AND u.email = 'erik@boka.se'
ON CONFLICT DO NOTHING;

INSERT INTO gym_classes (class_type_id, instructor_id, start_time, end_time, capacity)
SELECT ct.id, u.id, CURRENT_DATE + interval '3 days 12:00:00', CURRENT_DATE + interval '3 days 13:00:00', ct.default_capacity
FROM class_types ct, users u WHERE ct.name = 'Yoga' AND u.email = 'anna@boka.se'
ON CONFLICT DO NOTHING;

-- 5. Seed initial bookings
INSERT INTO bookings (user_id, gym_class_id, status)
SELECT u.id, gc.id, 'CONFIRMED'
FROM users u, gym_classes gc, class_types ct
WHERE u.email = 'karl@example.com' 
  AND gc.class_type_id = ct.id 
  AND ct.name = 'Yoga' 
  AND gc.start_time = CURRENT_DATE + interval '1 day 07:00:00'
ON CONFLICT DO NOTHING;

INSERT INTO bookings (user_id, gym_class_id, status)
SELECT u.id, gc.id, 'CONFIRMED'
FROM users u, gym_classes gc, class_types ct
WHERE u.email = 'lisa@example.com' 
  AND gc.class_type_id = ct.id 
  AND ct.name = 'Yoga' 
  AND gc.start_time = CURRENT_DATE + interval '1 day 07:00:00'
ON CONFLICT DO NOTHING;

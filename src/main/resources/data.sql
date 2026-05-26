INSERT INTO departments (department_id, department_name, hod_name) VALUES
(1, 'CSE', 'Dr. Kavya Rao'),
(2, 'AI & DS', 'Dr. Arjun Menon'),
(3, 'IT', 'Dr. Sneha Reddy'),
(4, 'ECE', 'Dr. Meera Nair'),
(5, 'Mechanical', 'Dr. Vikram Shah'),
(6, 'Civil', 'Dr. Nandini Iyer')
ON DUPLICATE KEY UPDATE department_name=VALUES(department_name), hod_name=VALUES(hod_name);

INSERT INTO courses (course_id, course_name, department, duration, total_fee, available_seats) VALUES
(1, 'B.Tech Computer Science Engineering', 'CSE', '4 Years', 125000, 120),
(2, 'B.Tech Artificial Intelligence and Data Science', 'AI & DS', '4 Years', 140000, 90),
(3, 'B.Tech Information Technology', 'IT', '4 Years', 115000, 100),
(4, 'B.Tech Electronics and Communication', 'ECE', '4 Years', 110000, 110),
(5, 'B.Tech Mechanical Engineering', 'Mechanical', '4 Years', 95000, 80),
(6, 'B.Tech Civil Engineering', 'Civil', '4 Years', 90000, 75)
ON DUPLICATE KEY UPDATE total_fee=VALUES(total_fee), available_seats=VALUES(available_seats);

INSERT INTO admin (admin_id, username, password, role) VALUES
(1, 'admin', '$2a$10$gThpIWByVn43ahBwJjj56.h87jdfmEuYsDoP1vZ9HB4rjTNctzqx.', 'ADMIN')
ON DUPLICATE KEY UPDATE username=VALUES(username), role=VALUES(role);

INSERT INTO hostel (hostel_id, hostel_name, gender_type, total_rooms, available_rooms) VALUES
(1, 'Krishna Bhavan', 'MALE', 120, 120),
(2, 'Vivekananda Block', 'MALE', 100, 100),
(3, 'APJ Abdul Kalam Hostel', 'MALE', 90, 90),
(4, 'Lotus Block', 'FEMALE', 110, 110),
(5, 'Saraswati Hostel', 'FEMALE', 100, 100)
ON DUPLICATE KEY UPDATE hostel_name=VALUES(hostel_name), gender_type=VALUES(gender_type), total_rooms=VALUES(total_rooms);

INSERT INTO hostel_rooms (room_id, hostel_id, room_type, room_number, sharing_type, ac_type, room_fee, availability_status) VALUES
(1, 1, '1 Sharing AC', 'KB-101', '1 Sharing', 'AC', 120000, 'AVAILABLE'),
(2, 1, '2 Sharing AC', 'KB-102', '2 Sharing', 'AC', 95000, 'AVAILABLE'),
(3, 1, '3 Sharing Non AC', 'KB-201', '3 Sharing', 'NON_AC', 75000, 'AVAILABLE'),
(4, 1, '4 Sharing Non AC', 'KB-202', '4 Sharing', 'NON_AC', 60000, 'AVAILABLE'),
(5, 2, '1 Sharing AC', 'VB-101', '1 Sharing', 'AC', 120000, 'AVAILABLE'),
(6, 2, '2 Sharing AC', 'VB-102', '2 Sharing', 'AC', 95000, 'AVAILABLE'),
(7, 3, '3 Sharing Non AC', 'AK-201', '3 Sharing', 'NON_AC', 75000, 'AVAILABLE'),
(8, 3, '4 Sharing Non AC', 'AK-202', '4 Sharing', 'NON_AC', 60000, 'AVAILABLE'),
(9, 4, '1 Sharing AC', 'LB-101', '1 Sharing', 'AC', 120000, 'AVAILABLE'),
(10, 4, '2 Sharing AC', 'LB-102', '2 Sharing', 'AC', 95000, 'AVAILABLE'),
(11, 4, '3 Sharing Non AC', 'LB-201', '3 Sharing', 'NON_AC', 75000, 'AVAILABLE'),
(12, 5, '4 Sharing Non AC', 'SH-202', '4 Sharing', 'NON_AC', 60000, 'AVAILABLE')
ON DUPLICATE KEY UPDATE room_number=VALUES(room_number), room_fee=VALUES(room_fee);

INSERT INTO announcements (announcement_id, title, description, posted_date) VALUES
(1, 'Admissions Open', 'B.Tech admissions for the academic year are now open. Apply through the portal.', CURRENT_TIMESTAMP),
(2, 'Semester Fee Window', 'Students can pay semester fees online before the due date to avoid late fees.', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description);

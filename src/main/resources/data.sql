-- 1. Specialty
INSERT INTO specialties (id, name) VALUES
  (2, 'Mathematics'),
  (3, 'Physics'),
  (4, 'Chemistry'),
  (5, 'Biology'),
  (6, 'Economics'),
  (7, 'History'),
  (8, 'Philosophy'),
  (9, 'Engineering'),
  (10, 'Literature');

-- 2. Group
INSERT INTO academic_groups (id, group_number, specialty_id) VALUES
  (2, 'MATH101', 2),
  (3, 'PHY101', 3),
  (4, 'CHEM101', 4),
  (5, 'BIO101', 5),
  (6, 'ECO101', 6),
  (7, 'HIST101', 7),
  (8, 'PHIL101', 8),
  (9, 'ENG101', 9),
  (10, 'LIT101', 10);

-- 3. User
INSERT INTO users (id, email, password, role, first_name, last_name, is_active) VALUES
  (3, 'student3@example.com', 'pass', 'STUDENT', 'Murad', 'Huseynov', true),
  (4, 'student4@example.com', 'pass', 'STUDENT', 'Nigar', 'Rahimova', true),
  (5, 'student5@example.com', 'pass', 'STUDENT', 'Kamran', 'Karimov', true),
  (6, 'student6@example.com', 'pass', 'STUDENT', 'Leyla', 'Guliyeva', true),
  (7, 'student7@example.com', 'pass', 'STUDENT', 'Elvin', 'Ismayilov', true),
  (8, 'student8@example.com', 'pass', 'STUDENT', 'Sabina', 'Aliyeva', true),
  (9, 'student9@example.com', 'pass', 'STUDENT', 'Rashad', 'Mammadov', true),
  (10, 'student10@example.com', 'pass', 'STUDENT', 'Sevinj', 'Huseynova', true),
  (11, 'teacher1@example.com', 'pass', 'TEACHER', 'Togrul', 'Aliyev', true),
  (12, 'teacher2@example.com', 'pass', 'TEACHER', 'Ulvi', 'Mammadov', true),
  (13, 'teacher3@example.com', 'pass', 'TEACHER', 'Zaur', 'Huseynov', true),
  (14, 'teacher4@example.com', 'pass', 'TEACHER', 'Ilham', 'Rahimov', true),
  (15, 'teacher5@example.com', 'pass', 'TEACHER', 'Samir', 'Karimov', true),
  (16, 'teacher6@example.com', 'pass', 'TEACHER', 'Nurlan', 'Guliyev', true),
  (17, 'teacher7@example.com', 'pass', 'TEACHER', 'Elmar', 'Ismayilov', true),
  (18, 'teacher8@example.com', 'pass', 'TEACHER', 'Amina', 'Aliyeva', true),
  (19, 'teacher9@example.com', 'pass', 'TEACHER', 'Rauf', 'Mammadov', true),
  (20, 'teacher10@example.com', 'pass', 'TEACHER', 'Narmin', 'Huseynova', true);

-- 4. Student
INSERT INTO students (id, user_id, student_number, group_id) VALUES
  (2, 2, 'S1002', 2),
  (3, 3, 'S1003', 3),
  (4, 4, 'S1004', 4),
  (5, 5, 'S1005', 5),
  (6, 6, 'S1006', 6),
  (7, 7, 'S1007', 7),
  (8, 8, 'S1008', 8),
  (9, 9, 'S1009', 9),
  (10, 10, 'S1010', 10);

-- 5. Teacher
INSERT INTO teachers (id, user_id, department) VALUES
  (1, 11, 'Computer Science'),
  (2, 12, 'Mathematics'),
  (3, 13, 'Physics'),
  (4, 14, 'Chemistry'),
  (5, 15, 'Biology'),
  (6, 16, 'Economics'),
  (7, 17, 'History'),
  (8, 18, 'Philosophy'),
  (9, 19, 'Engineering'),
  (10, 20, 'Literature');

-- 6. Subject
INSERT INTO subjects (id, name, credits) VALUES
  (1, 'Algorithms', 6),
  (2, 'Calculus', 5),
  (3, 'Mechanics', 5),
  (4, 'Organic Chemistry', 4),
  (5, 'Genetics', 4),
  (6, 'Microeconomics', 5),
  (7, 'World History', 3),
  (8, 'Logic', 3),
  (9, 'Thermodynamics', 5),
  (10, 'Poetry', 2);

-- 7. TeacherGroupSubject
INSERT INTO teacher_group_subjects (id, teacher_id, group_id, subject_id) VALUES
  (1, 1, 1, 1),
  (2, 2, 2, 2),
  (3, 3, 3, 3),
  (4, 4, 4, 4),
  (5, 5, 5, 5),
  (6, 6, 6, 6),
  (7, 7, 7, 7),
  (8, 8, 8, 8),
  (9, 9, 9, 9),
  (10, 10, 10, 10);

-- 8. Lesson
INSERT INTO lessons (id, teacher_group_subject_id, start_time, end_time, is_active) VALUES
  (1, 1, '2026-04-21T09:00:00', '2026-04-21T10:30:00', true),
  (2, 2, '2026-04-21T10:00:00', '2026-04-21T11:30:00', true),
  (3, 3, '2026-04-21T11:00:00', '2026-04-21T12:30:00', true),
  (4, 4, '2026-04-21T12:00:00', '2026-04-21T13:30:00', true),
  (5, 5, '2026-04-21T13:00:00', '2026-04-21T14:30:00', true),
  (6, 6, '2026-04-21T14:00:00', '2026-04-21T15:30:00', true),
  (7, 7, '2026-04-21T15:00:00', '2026-04-21T16:30:00', true),
  (8, 8, '2026-04-21T16:00:00', '2026-04-21T17:30:00', true),
  (9, 9, '2026-04-21T17:00:00', '2026-04-21T18:30:00', true),
  (10, 10, '2026-04-21T18:00:00', '2026-04-21T19:30:00', true);

-- 9. Grade
INSERT INTO grade (id, student_id, subject_id, teacher_id, attendance_score, seminar_score, col1, col2, col3, exam_score, total_score, status) VALUES
  (1, 1, 1, 1, 10, 9, 8, 7, 6, 45, 85, 'PASSED'),
  (2, 2, 2, 2, 9, 8, 7, 6, 5, 40, 75, 'PASSED'),
  (3, 3, 3, 3, 8, 7, 6, 5, 4, 35, 65, 'PASSED'),
  (4, 4, 4, 4, 7, 6, 5, 4, 3, 30, 55, 'PASSED'),
  (5, 5, 5, 5, 6, 5, 4, 3, 2, 25, 45, 'FAILED_BY_TOTAL'),
  (6, 6, 6, 6, 5, 4, 3, 2, 1, 20, 35, 'FAILED_BY_TOTAL'),
  (7, 7, 7, 7, 10, 10, 10, 10, 10, 50, 100, 'PASSED'),
  (8, 8, 8, 8, 9, 9, 9, 9, 9, 45, 90, 'PASSED'),
  (9, 9, 9, 9, 8, 8, 8, 8, 8, 40, 80, 'PASSED'),
  (10, 10, 10, 10, 7, 7, 7, 7, 7, 35, 70, 'PASSED');

-- 10. Attendance
INSERT INTO attendance (id, lesson_id, student_id, status, marked_at, last_modified_at, remarks) VALUES
  (1, 1, 1, 'PRESENT', '2026-04-21T09:05:00', '2026-04-21T09:05:00', 'On time'),
  (2, 2, 2, 'ABSENT', '2026-04-21T10:10:00', '2026-04-21T10:10:00', 'Sick'),
  (3, 3, 3, 'PRESENT', '2026-04-21T11:15:00', '2026-04-21T11:15:00', 'Late'),
  (4, 4, 4, 'WARNING', '2026-04-21T12:20:00', '2026-04-21T12:20:00', 'Disruptive'),
  (5, 5, 5, 'PRESENT', '2026-04-21T13:25:00', '2026-04-21T13:25:00', 'Good'),
  (6, 6, 6, 'ABSENT', '2026-04-21T14:30:00', '2026-04-21T14:30:00', 'Family issue'),
  (7, 7, 7, 'PRESENT', '2026-04-21T15:35:00', '2026-04-21T15:35:00', 'Excellent'),
  (8, 8, 8, 'WARNING', '2026-04-21T16:40:00', '2026-04-21T16:40:00', 'Cheating'),
  (9, 9, 9, 'PRESENT', '2026-04-21T17:45:00', '2026-04-21T17:45:00', 'Participated'),
  (10, 10, 10, 'ABSENT', '2026-04-21T18:50:00', '2026-04-21T18:50:00', 'Travel');


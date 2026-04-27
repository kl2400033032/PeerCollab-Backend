INSERT INTO users (name, email, password, role, created_at)
SELECT 'PeerCollab Admin', 'admin@peercollab.com', '$2a$10$bcwFlO8h575kQFPvwWWvC.Q7Uhtvn7Pc3EIHgLVpYwPcGI0wf1YFS', 'ADMIN', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@peercollab.com'
);

INSERT INTO users (name, email, password, role, created_at)
SELECT 'Student One', 'student1@peercollab.com', '$2a$10$eiO3RPX9rrWQkqOl32LFPOEb8CDllN9/8Iw2iekrltSs8Hc3aGqfi', 'STUDENT', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'student1@peercollab.com'
);

INSERT INTO users (name, email, password, role, created_at)
SELECT 'Student Two', 'student2@peercollab.com', '$2a$10$eiO3RPX9rrWQkqOl32LFPOEb8CDllN9/8Iw2iekrltSs8Hc3aGqfi', 'STUDENT', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'student2@peercollab.com'
);

INSERT INTO assignments (title, description, due_date, created_by_id, assigned_student_id, created_at)
SELECT
    'Capstone Sprint Review',
    'Prepare the final sprint deliverables and collaboration artifacts for peer evaluation.',
    DATE_ADD(CURDATE(), INTERVAL 14 DAY),
    (SELECT id FROM users WHERE email = 'admin@peercollab.com'),
    (SELECT id FROM users WHERE email = 'student1@peercollab.com'),
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM assignments WHERE title = 'Capstone Sprint Review'
);

INSERT INTO projects (
    title,
    description,
    student_name,
    status,
    student_id,
    assignment_id,
    created_at,
    updated_at
)
SELECT
    'AI Study Planner',
    'A collaboration platform module that helps students plan revision schedules, manage tasks, and review each other''s progress with structured milestones.',
    'Student One',
    'UNDER_REVIEW',
    (SELECT id FROM users WHERE email = 'student1@peercollab.com'),
    (SELECT id FROM assignments WHERE title = 'Capstone Sprint Review'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE title = 'AI Study Planner'
);

INSERT INTO projects (
    title,
    description,
    student_name,
    status,
    student_id,
    assignment_id,
    created_at,
    updated_at
)
SELECT
    'Collaborative Code Review Hub',
    'A lightweight peer review dashboard for classroom submissions with notifications, analytics, and rubric-based assessment support.',
    'Student Two',
    'SUBMITTED',
    (SELECT id FROM users WHERE email = 'student2@peercollab.com'),
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE title = 'Collaborative Code Review Hub'
);

INSERT INTO projects (
    title,
    description,
    student_name,
    status,
    student_id,
    assignment_id,
    created_at,
    updated_at
)
SELECT
    'PeerCollab Mobile Companion',
    'A companion idea focused on mobile-friendly activity feeds, push-style alerts, and accessible project discussion spaces.',
    'Student One',
    'COMPLETED',
    (SELECT id FROM users WHERE email = 'student1@peercollab.com'),
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE title = 'PeerCollab Mobile Companion'
);

INSERT INTO reviews (reviewer_name, feedback, rating, project_id, reviewer_id, created_at)
SELECT
    'Student Two',
    'The planner flow is clear and the milestone breakdown feels strong. Add more analytics for progress trends.',
    4,
    (SELECT id FROM projects WHERE title = 'AI Study Planner'),
    (SELECT id FROM users WHERE email = 'student2@peercollab.com'),
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM reviews
    WHERE project_id = (SELECT id FROM projects WHERE title = 'AI Study Planner')
      AND reviewer_id = (SELECT id FROM users WHERE email = 'student2@peercollab.com')
);

INSERT INTO reviews (reviewer_name, feedback, rating, project_id, reviewer_id, created_at)
SELECT
    'Student One',
    'The dashboard concept is polished. Consider adding downloadable summary reports for teachers.',
    5,
    (SELECT id FROM projects WHERE title = 'Collaborative Code Review Hub'),
    (SELECT id FROM users WHERE email = 'student1@peercollab.com'),
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM reviews
    WHERE project_id = (SELECT id FROM projects WHERE title = 'Collaborative Code Review Hub')
      AND reviewer_id = (SELECT id FROM users WHERE email = 'student1@peercollab.com')
);

INSERT INTO comments (author_name, message, project_id, author_id, created_at)
SELECT
    'Student One',
    'Let us refine the dashboard cards before the presentation.',
    (SELECT id FROM projects WHERE title = 'AI Study Planner'),
    (SELECT id FROM users WHERE email = 'student1@peercollab.com'),
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM comments
    WHERE project_id = (SELECT id FROM projects WHERE title = 'AI Study Planner')
      AND message = 'Let us refine the dashboard cards before the presentation.'
);

INSERT INTO comments (author_name, message, project_id, author_id, created_at)
SELECT
    'Student Two',
    'I can add a cleaner review summary widget and help with the final screenshots.',
    (SELECT id FROM projects WHERE title = 'AI Study Planner'),
    (SELECT id FROM users WHERE email = 'student2@peercollab.com'),
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM comments
    WHERE project_id = (SELECT id FROM projects WHERE title = 'AI Study Planner')
      AND message = 'I can add a cleaner review summary widget and help with the final screenshots.'
);

INSERT INTO notifications (recipient_id, type, title, message, link, is_read, created_at)
SELECT
    (SELECT id FROM users WHERE email = 'student1@peercollab.com'),
    'REVIEW_RECEIVED',
    'Demo review received',
    'Student Two reviewed your project "AI Study Planner".',
    CONCAT('/projects/', (SELECT id FROM projects WHERE title = 'AI Study Planner')),
    b'0',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM notifications WHERE title = 'Demo review received'
);

INSERT INTO activity_logs (user_id, type, description, metadata, created_at)
SELECT
    (SELECT id FROM users WHERE email = 'admin@peercollab.com'),
    'ASSIGNMENT_CREATED',
    'Created the Capstone Sprint Review assignment',
    'assignmentTitle=Capstone Sprint Review',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM activity_logs WHERE description = 'Created the Capstone Sprint Review assignment'
);

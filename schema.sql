CREATE DATABASE IF NOT EXISTS peercollabdb;
USE peercollabdb;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'STUDENT'))
);

CREATE TABLE IF NOT EXISTS assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(140) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    due_date DATE NOT NULL,
    created_by_id BIGINT NOT NULL,
    assigned_student_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_assignments PRIMARY KEY (id),
    CONSTRAINT fk_assignments_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT fk_assignments_assigned_student FOREIGN KEY (assigned_student_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(140) NOT NULL,
    description VARCHAR(2500) NOT NULL,
    student_name VARCHAR(120) NOT NULL,
    status VARCHAR(30) NOT NULL,
    original_file_name VARCHAR(255) NULL,
    stored_file_name VARCHAR(255) NULL,
    file_content_type VARCHAR(120) NULL,
    file_size BIGINT NULL,
    student_id BIGINT NOT NULL,
    assignment_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT fk_projects_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_projects_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    CONSTRAINT uk_projects_assignment UNIQUE (assignment_id),
    CONSTRAINT chk_projects_status CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'COMPLETED'))
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reviewer_name VARCHAR(120) NOT NULL,
    feedback VARCHAR(2000) NOT NULL,
    rating INT NOT NULL,
    project_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_reviews PRIMARY KEY (id),
    CONSTRAINT fk_reviews_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uk_reviews_project_reviewer UNIQUE (project_id, reviewer_id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    author_name VARCHAR(120) NOT NULL,
    message VARCHAR(1500) NOT NULL,
    project_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message VARCHAR(500) NOT NULL,
    link VARCHAR(255) NULL,
    is_read BIT(1) NOT NULL DEFAULT b'0',
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    description VARCHAR(200) NOT NULL,
    metadata VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_activity_logs PRIMARY KEY (id),
    CONSTRAINT fk_activity_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_projects_title ON projects(title);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_student_name ON projects(student_name);
CREATE INDEX idx_projects_student_id ON projects(student_id);
CREATE INDEX idx_projects_assignment_id ON projects(assignment_id);
CREATE INDEX idx_reviews_project_id ON reviews(project_id);
CREATE INDEX idx_reviews_reviewer_id ON reviews(reviewer_id);
CREATE INDEX idx_comments_project_id ON comments(project_id);
CREATE INDEX idx_comments_author_id ON comments(author_id);
CREATE INDEX idx_assignments_student_id ON assignments(assigned_student_id);
CREATE INDEX idx_assignments_created_by_id ON assignments(created_by_id);
CREATE INDEX idx_notifications_recipient_created_at ON notifications(recipient_id, created_at);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_activity_logs_user_created_at ON activity_logs(user_id, created_at);
CREATE INDEX idx_activity_logs_type ON activity_logs(type);

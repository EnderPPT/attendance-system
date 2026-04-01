DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS course_selection CASCADE;
DROP TABLE IF EXISTS course CASCADE;
DROP TABLE IF EXISTS sys_user CASCADE;

CREATE DATABASE attendance_system ENCODING 'UTF8';

CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) DEFAULT 'STUDENT',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.id IS '主键';
COMMENT ON COLUMN sys_user.role IS '角色:ADMIN/TEACHER/STUDENT';

CREATE TABLE course (
    course_id BIGSERIAL PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(50) NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    teacher_id BIGINT NOT NULL,
    classroom_name VARCHAR(50) NOT NULL,
    layout_rows SMALLINT NOT NULL,
    layout_cols SMALLINT NOT NULL,
    exclude_seats VARCHAR(200),
    weekday SMALLINT,
    start_week INT,
    end_week INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_teacher_id FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
);
COMMENT ON TABLE course IS '课程表';
COMMENT ON COLUMN course.layout_rows IS '教室行数';
COMMENT ON COLUMN course.layout_cols IS '教室列数';

CREATE TABLE course_selection (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    select_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_selection_course_id FOREIGN KEY (course_id) REFERENCES course(course_id),
    CONSTRAINT fk_course_selection_student_id FOREIGN KEY (student_id) REFERENCES sys_user(id),
    CONSTRAINT uk_course_selection UNIQUE (course_id, student_id)
);
COMMENT ON TABLE course_selection IS '选课表';

CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    check_in_time TIMESTAMP NOT NULL,
    seat_row SMALLINT NOT NULL,
    seat_col SMALLINT NOT NULL,
    status VARCHAR(20) DEFAULT 'NORMAL',
    ip VARCHAR(45),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_course_id FOREIGN KEY (course_id) REFERENCES course(course_id),
    CONSTRAINT fk_attendance_student_id FOREIGN KEY (student_id) REFERENCES sys_user(id)
);
COMMENT ON TABLE attendance IS '考勤记录表';
COMMENT ON COLUMN attendance.status IS '状态: NORMAL正常/LATE迟到/EARLY早退/ABSENT缺勤';

INSERT INTO sys_user (username, password, real_name, role) VALUES
('admin', '123456', '张管理', 'ADMIN'),
('t_wang', '123456', '王老师', 'TEACHER'),
('s_001', '123456', '赵同学', 'STUDENT'),
('s_002', '123456', '钱同学', 'STUDENT'),
('s_003', '123456', '孙同学', 'STUDENT'),
('s_004', '123456', '李同学', 'STUDENT'),
('s_005', '123456', '周同学', 'STUDENT');

INSERT INTO course (course_code, course_name, class_name, teacher_id, classroom_name, layout_rows, layout_cols, exclude_seats, weekday, start_week, end_week) VALUES
('CS101', 'Java后端开发', '计科1班', 2, '第一教学楼-101', 8, 10, '1,1;1,2', 3, 1, 16),
('CS102', '数据库原理', '计科1班', 2, '第二教学楼-205', 10, 10, '', 5, 1, 16);

INSERT INTO course_selection (course_id, student_id) VALUES
(1, 3), (1, 4), (1, 5), (1, 6), (1, 7);

INSERT INTO attendance (course_id, student_id, check_in_time, seat_row, seat_col, status, ip) VALUES
(1, 3, '2026-04-01 08:50:00', 2, 3, 'NORMAL', '192.168.1.10'),
(1, 4, '2026-04-01 08:52:00', 2, 4, 'NORMAL', '192.168.1.11'),
(1, 5, '2026-04-01 09:15:00', 3, 1, 'LATE', '192.168.1.12'),
(1, 7, '2026-04-01 08:58:00', 4, 5, 'NORMAL', '192.168.1.14');
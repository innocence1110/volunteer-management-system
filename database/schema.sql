-- 志愿活动管理系统 数据库初始化脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS volunteer_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE volunteer_system;


-- 1. 用户表 (users)
DROP TABLE IF EXISTS check_ins;
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS activities;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    phone       VARCHAR(20)  NOT NULL COMMENT '电话号码',
    account     VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    role        ENUM('admin', 'volunteer') NOT NULL DEFAULT 'volunteer' COMMENT '角色：admin-管理员, volunteer-志愿者',
    major       VARCHAR(100) DEFAULT NULL COMMENT '专业',
    age         INT          DEFAULT NULL COMMENT '年龄',
    student_id  VARCHAR(50)  DEFAULT NULL COMMENT '学号',
    points      INT          DEFAULT 0 COMMENT '积分',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_account (account),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- 2. 活动表 (activities)
CREATE TABLE activities (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
    name            VARCHAR(200) NOT NULL COMMENT '活动名称',
    description     TEXT         DEFAULT NULL COMMENT '活动描述',
    start_time      DATETIME     NOT NULL COMMENT '活动开始时间',
    end_time        DATETIME     NOT NULL COMMENT '活动结束时间',
    location        VARCHAR(200) NOT NULL COMMENT '活动地点',
    max_participants INT         NOT NULL DEFAULT 0 COMMENT '招募人数上限',
    check_in_type   ENUM('button', 'code', 'image') NOT NULL DEFAULT 'button' COMMENT '签到方式：button-按钮, code-数字码, image-图片',
    check_in_code   VARCHAR(10)  DEFAULT NULL COMMENT '数字码签到验证码',
    status          ENUM('pending', 'ongoing', 'ended', 'cancelled') DEFAULT 'ongoing' COMMENT '活动状态',
    points_reward   INT          DEFAULT 10 COMMENT '完成活动奖励积分',
    publisher_id    BIGINT       NOT NULL COMMENT '发布者ID',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_publisher (publisher_id),
    CONSTRAINT fk_activity_publisher FOREIGN KEY (publisher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';


-- 3. 报名表 (registrations)
CREATE TABLE registrations (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
    user_id      BIGINT NOT NULL COMMENT '志愿者ID',
    activity_id  BIGINT NOT NULL COMMENT '活动ID',
    status       ENUM('registered', 'cancelled') DEFAULT 'registered' COMMENT '报名状态',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    UNIQUE KEY uk_user_activity (user_id, activity_id),
    INDEX idx_activity (activity_id),
    CONSTRAINT fk_reg_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名表';


-- 4. 签到表 (check_ins)
CREATE TABLE check_ins (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '签到ID',
    user_id      BIGINT       NOT NULL COMMENT '志愿者ID',
    activity_id  BIGINT       NOT NULL COMMENT '活动ID',
    check_in_type ENUM('button', 'code', 'image') NOT NULL COMMENT '签到方式',
    check_in_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
    gps_address  VARCHAR(500) DEFAULT NULL COMMENT 'GPS定位地址（按钮签到）',
    image_path   VARCHAR(500) DEFAULT NULL COMMENT '签到图片路径（图片签到）',
    UNIQUE KEY uk_user_activity_checkin (user_id, activity_id),
    INDEX idx_activity (activity_id),
    CONSTRAINT fk_checkin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkin_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到表';


-- 5. 通知表 (notifications)
CREATE TABLE notifications (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id      BIGINT       NOT NULL COMMENT '接收者ID',
    title        VARCHAR(200) NOT NULL COMMENT '消息标题',
    content      TEXT         NOT NULL COMMENT '消息内容',
    is_read      TINYINT(1)   DEFAULT 0 COMMENT '是否已读：0-未读, 1-已读',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    INDEX idx_user (user_id),
    INDEX idx_read (is_read),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';


-- 6. 插入初始管理员账号
-- 密码: admin123 (BCrypt加密)
INSERT INTO users (name, phone, account, password, role)
VALUES ('系统管理员', '13800000000', 'admin',
        '$2a$10$G2EJ7Pal3ePsmkuq66Cz0OAvi.xZhcDSsyhKbZtOQSqzutm63WSDK',
        'admin');

-- 密码: 123456
INSERT INTO users (name, phone, account, password, role, major, age, student_id)
VALUES ('测试志愿者', '13800000001', 'volunteer',
        '$2a$10$EgeWVoUkD6capH6RolAOYuAO/jqbwnQxqgYGVMuCTL6nkHmu05rOa',
        'volunteer', '计算机科学', 20, '20210001');

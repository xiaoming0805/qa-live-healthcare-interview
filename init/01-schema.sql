-- ============================================================
-- 医生表结构
-- 数据来源：web/qa-web/src/data/doctor-user-list.json
-- 说明：id 沿用原 JSON 中的业务主键（doc001...），
--       以便 question-list.json 中已有的 doctorId 引用继续成立
-- ============================================================

USE qa_healthcare;

CREATE TABLE IF NOT EXISTS doctor (
    id          VARCHAR(32)  NOT NULL COMMENT '医生业务主键，沿用前端既有 doc001 形式',
    username    VARCHAR(64)  NOT NULL COMMENT '登录用户名',
    password    VARCHAR(128) NOT NULL COMMENT '登录密码（演示用明文，生产应存 BCrypt 哈希）',
    name        VARCHAR(64)  NOT NULL COMMENT '医生姓名',
    title       VARCHAR(64)           COMMENT '职称，如主任医师',
    department  VARCHAR(64)           COMMENT '所属科室',
    avatar      VARCHAR(512)          COMMENT '头像地址',
    experience  VARCHAR(128)          COMMENT '从业经验描述',
    specialties JSON                  COMMENT '擅长领域，JSON 数组',
    is_active   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否开放诊室',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_doctor_username (username),
    KEY idx_doctor_is_active (is_active)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='医生信息';

-- ============================================================
-- 患者表结构
-- 数据来源：web/qa-web/src/data/patient-user.json
-- 说明：id 沿用原 JSON 中的业务主键（patient001...），
--       以便 question-list.json 中已有的 patientId 引用继续成立
-- ============================================================

USE qa_healthcare;

CREATE TABLE IF NOT EXISTS patient (
    id         VARCHAR(32)  NOT NULL COMMENT '患者业务主键，沿用前端既有 patient001 形式',
    username   VARCHAR(64)  NOT NULL COMMENT '登录用户名',
    password   VARCHAR(128) NOT NULL COMMENT '登录密码（演示用明文，生产应存 BCrypt 哈希）',
    name       VARCHAR(64)  NOT NULL COMMENT '患者姓名',
    birthday   DATE                  COMMENT '生日',
    phone      VARCHAR(32)           COMMENT '联系电话',
    gender     VARCHAR(8)            COMMENT '性别',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_patient_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='问诊用户（患者）';

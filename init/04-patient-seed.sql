-- ============================================================
-- 患者种子数据
-- 由 web/qa-web/src/data/patient-user.json 迁移而来，共 5 条
-- 原 JSON 中不含登录凭据，此处补充：username = id，password = 123456
-- ============================================================

USE qa_healthcare;

INSERT INTO patient (id, username, password, name, birthday, phone, gender)
VALUES ('patient001', 'patient001', '123456', '赵明', '1985-03-15', '138****1234', '男'),
       ('patient002', 'patient002', '123456', '孙丽', '1990-07-22', '139****5678', '女'),
       ('patient003', 'patient003', '123456', '周杰', '1978-11-08', '137****9012', '男'),
       ('patient004', 'patient004', '123456', '吴芳', '1995-05-20', '136****3456', '女'),
       ('patient005', 'patient005', '123456', '郑浩', '1988-09-12', '135****7890', '男')
ON DUPLICATE KEY UPDATE id = id;

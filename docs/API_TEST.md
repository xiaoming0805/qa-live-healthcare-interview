# 医生接口测试流程（API_TEST.md）

> 本文档用于指导 AI 或工程师对「需求2：医生数据迁移至 MySQL 并提供医生列表 API」所涉及的接口进行测试，并据此生成测试报告。
> 执行方式：按 TC 编号顺序逐条执行，记录实际输出，与「预期结果」比对，最后按文末模版产出测试报告。

---

## 1. 测试目标与范围

| 项目 | 内容 |
| --- | --- |
| 被测服务 | `qa-service-user`（http://localhost:8080） |
| 被测接口 | `/api/doctors` 系列 4 个接口 + `/api/patients` 系列 3 个接口 |
| 依赖组件 | MySQL 8.0（docker）、phpMyAdmin |
| 测试类型 | 接口功能测试、安全回归测试、端到端联调测试 |
| 不在范围 | 首页 i18n（需求1）、患者登录（需求3）、性能压测 |

---

## 2. 前置条件

| 项 | 要求 |
| --- | --- |
| JDK | 17 及以上（`java -version` 确认） |
| Docker | 已安装且 daemon 运行中（`docker info`） |
| Maven | 3.8+，且 `JAVA_HOME` 指向 JDK 17+ |
| 前端（可选） | Node 18+，用于 TC-15 的端到端验证 |

> ⚠️ 注意1：若本机 `mvn -v` 显示的 Java 版本为 1.8，需先切换 `JAVA_HOME`，否则 Spring Boot 3.5 无法编译。
>
> ⚠️ 注意2：`init/*.sql` 仅在 MySQL **数据卷为空**时自动执行。若容器此前已初始化过（例如只跑过医生模块），新增的 `init/03-patient-schema.sql` 与 `init/04-patient-seed.sql` **不会**自动运行，需先 `docker compose down -v` 清空数据卷再 `up -d`，或手动在 phpMyAdmin 中执行这两个脚本。
>
> ⚠️ 注意3：若使用**既有 MySQL 实例**（不通过 `docker-compose.yml` 启动）：
> 1. 在 `server/qa-service-user/src/main/resources/application.properties` 中配置连接信息（或通过 `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` 环境变量覆盖，见该文件注释）；
> 2. 手动在该实例上执行 `init/01` ~ `init/04` 四个脚本；
> 3. 本文档中所有 `docker exec qa-mysql ...` 形式的元数据类用例（TC-01/02/03/16/17）改用 MySQL 客户端或数据库工具执行等价 SQL；其中 TC-01 可直接跳过。

---

## 3. 环境准备

```bash
# 1) 启动数据库与 phpMyAdmin（在仓库根目录执行）
docker compose up -d

# 2) 等待 mysql 容器变为 healthy
docker compose ps

# 3) 启动后端
cd server/qa-service-user
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run

# 4) 确认服务已就绪
curl -s http://localhost:8080/actuator/health
```

**预期**：`docker compose ps` 中 `qa-mysql` 状态为 `healthy`，`curl` 返回 `{"status":"UP"}`。

---

## 4. 测试用例

### TC-01 数据库容器健康检查

```bash
docker compose ps --format "table {{.Name}}\t{{.Status}}"
```

**预期结果**：`qa-mysql` 与 `qa-phpmyadmin` 均为 `Up`，且 mysql 显示 `(healthy)`。

---

### TC-02 表结构校验

```bash
docker exec qa-mysql mysql -uroot -proot123 -e "USE qa_healthcare; SHOW CREATE TABLE doctor\G"
```

**预期结果**：
- 表 `doctor` 存在
- 包含字段 `id, username, password, name, title, department, avatar, experience, specialties, is_active, created_at, updated_at`
- `specialties` 类型为 `json`
- 存在 `uk_doctor_username` 唯一键
- 字符集为 `utf8mb4`

---

### TC-03 种子数据校验

```bash
docker exec qa-mysql mysql -uroot -proot123 -e \
  "USE qa_healthcare; SELECT COUNT(*) AS total, SUM(is_active) AS active FROM doctor;"
```

**预期结果**：`total = 5`，`active = 4`（`doc004` 刘敏医生为下架状态）。

---

### TC-04 查询全部医生

```bash
curl -s http://localhost:8080/api/doctors
```

**预期结果**：
- HTTP 200
- `code = 0`，`message = "success"`
- `data` 为长度 5 的数组
- 首位为 `doc001 / 张伟医生 / 心内科`

---

### TC-05 仅查询开放诊室的医生

```bash
curl -s "http://localhost:8080/api/doctors?active=true"
```

**预期结果**：`data` 长度为 4，**不含** `doc004`（刘敏医生）。

---

### TC-06 【安全回归】响应中不得包含 password

```bash
curl -s http://localhost:8080/api/doctors | grep -c '"password"'
```

**预期结果**：输出 `0`。

> 该用例是本次改造的核心安全收益，必须通过。

---

### TC-07 specialties 为 JSON 数组

```bash
curl -s http://localhost:8080/api/doctors | grep -o '"specialties":\[[^]]*\]' | head -1
```

**预期结果**：输出形如 `"specialties":["高血压","冠心病","心律失常"]`，为数组而非字符串。

---

### TC-08 按 id 查询医生

```bash
curl -s http://localhost:8080/api/doctors/doc002
```

**预期结果**：HTTP 200，`data.id = doc002`，`data.name = 李娜医生`。

---

### TC-09 按不存在的 id 查询

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/doctors/doc999
curl -s http://localhost:8080/api/doctors/doc999
```

**预期结果**：HTTP `404`，响应体 `{ "code": 404, "message": "医生不存在: doc999", "data": null }`。

---

### TC-10 按用户名查询医生

```bash
curl -s http://localhost:8080/api/doctors/username/dr-zhang-wei
```

**预期结果**：HTTP 200，`data.id = doc001`。

---

### TC-11 按不存在的用户名查询

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/doctors/username/nobody
```

**预期结果**：HTTP `404`。

---

### TC-12 医生登录成功

```bash
curl -s -X POST http://localhost:8080/api/doctors/login \
  -H "Content-Type: application/json" \
  -d '{"username":"dr-zhang-wei","password":"123456"}'
```

**预期结果**：HTTP 200，`code = 0`，`data.id = doc001`，且 `data` 中**不含** `password`。

---

### TC-13 医生登录密码错误

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/api/doctors/login \
  -H "Content-Type: application/json" \
  -d '{"username":"dr-zhang-wei","password":"wrong-password"}'
```

**预期结果**：HTTP `401`。

---

### TC-14 【安全】登录失败不泄露账号是否存在

```bash
curl -s -X POST http://localhost:8080/api/doctors/login \
  -H "Content-Type: application/json" -d '{"username":"dr-zhang-wei","password":"bad"}'
curl -s -X POST http://localhost:8080/api/doctors/login \
  -H "Content-Type: application/json" -d '{"username":"no-such-user","password":"bad"}'
```

**预期结果**：两次响应均为 401，且 `message` **完全一致**（均为「用户名或密码错误」），避免账号枚举。

---

### TC-15 【端到端】数据确实来自数据库

```bash
# 1) 把 doc004 上架
docker exec qa-mysql mysql -uroot -proot123 -e \
  "USE qa_healthcare; UPDATE doctor SET is_active = 1 WHERE id = 'doc004';"

# 2) 再次查询接口
curl -s "http://localhost:8080/api/doctors?active=true" | grep -c '"id"'

# 3) 还原
docker exec qa-mysql mysql -uroot -proot123 -e \
  "USE qa_healthcare; UPDATE doctor SET is_active = 0 WHERE id = 'doc004';"
```

**预期结果**：第 2 步输出 `5`（原为 4）。

**补充人工验证**：启动前端 `npm run dev`，打开 `http://localhost:5173/doctors`，页面应显示 5 张医生卡片；再重复第 1 步并刷新页面，「开放诊室」区域应多出刘敏医生。

---

## 4.2 患者模块用例（需求3）

### TC-16 患者表结构校验

```bash
docker exec qa-mysql mysql -uroot -proot123 -e "USE qa_healthcare; SHOW CREATE TABLE patient\G"
```

**预期结果**：
- 表 `patient` 存在
- 包含字段 `id, username, password, name, birthday, phone, gender, created_at, updated_at`
- `birthday` 类型为 `date`
- 存在 `uk_patient_username` 唯一键
- 字符集为 `utf8mb4`

---

### TC-17 患者种子数据校验

```bash
docker exec qa-mysql mysql -uroot -proot123 -e \
  "USE qa_healthcare; SELECT id, username, name, birthday, gender FROM patient ORDER BY id;"
```

**预期结果**：共 5 行，`username` 依次为 `patient001`~`patient005`，`name`/`birthday`/`gender` 与 `patient-user.json` 一致。

---

### TC-18 患者登录成功

```bash
curl -s -X POST http://localhost:8080/api/patients/login \
  -H "Content-Type: application/json" \
  -d '{"username":"patient001","password":"123456"}'

# 同时校验响应不含密码
curl -s -X POST http://localhost:8080/api/patients/login \
  -H "Content-Type: application/json" \
  -d '{"username":"patient001","password":"123456"}' | grep -c '"password"'
```

**预期结果**：HTTP 200，`code = 0`，`data.id = patient001`，`data.name = 赵明`；第二条命令输出 `0`。

---

### TC-19 患者登录密码错误

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/api/patients/login \
  -H "Content-Type: application/json" \
  -d '{"username":"patient001","password":"wrong-password"}'
```

**预期结果**：HTTP `401`。

---

### TC-20 【安全】患者登录不泄露账号是否存在

```bash
curl -s -X POST http://localhost:8080/api/patients/login \
  -H "Content-Type: application/json" -d '{"username":"patient001","password":"bad"}'
curl -s -X POST http://localhost:8080/api/patients/login \
  -H "Content-Type: application/json" -d '{"username":"no-such-user","password":"bad"}'
```

**预期结果**：两次响应均为 401，且 `message` **完全一致**（均为「用户名或密码错误」）。

---

### TC-21 患者注册成功

```bash
curl -s -X POST http://localhost:8080/api/patients/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice-test","password":"abc12345","name":"测试用户","birthday":"1990-05-20"}'
```

**预期结果**：HTTP 200，`code = 0`，`data.id` 形如 `patient006`，响应中不含 `password`。

> 说明：重复执行本用例会因用户名已存在而返回 409。如需重跑，先执行
> `docker exec qa-mysql mysql -uroot -proot123 -e "USE qa_healthcare; DELETE FROM patient WHERE username='alice-test';"`

---

### TC-22 患者注册用户名重复

先执行 TC-21，再重复执行同一条命令。

**预期结果**：HTTP `409`，`message = "用户名已存在"`。

---

### TC-23 患者注册参数缺失

```bash
curl -s -w "\nHTTP:%{http_code}\n" -X POST http://localhost:8080/api/patients/register \
  -H "Content-Type: application/json" \
  -d '{"username":"","password":"abc12345","name":"测试","birthday":"1990-01-01"}'
```

**预期结果**：HTTP `400`，`message = "用户名不能为空"`。

---

### TC-24 患者注册生日格式非法

```bash
curl -s -w "\nHTTP:%{http_code}\n" -X POST http://localhost:8080/api/patients/register \
  -H "Content-Type: application/json" \
  -d '{"username":"bob-test","password":"abc12345","name":"测试B","birthday":"1990/05/20"}'
```

**预期结果**：HTTP `400`，`message = "生日格式不正确，应为 yyyy-MM-dd"`。

---

### TC-25 【端到端】注册后可立即登录

依赖 TC-21 创建的账号。

```bash
curl -s -X POST http://localhost:8080/api/patients/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice-test","password":"abc12345"}'
```

**预期结果**：HTTP 200，`code = 0`，`data.username = alice-test`。证明注册已写库，而非停留在内存。

**补充人工验证**：打开 `http://localhost:5173/consultation`，用 `patient001 / 123456` 登录应进入问诊界面；用错误密码应提示「用户名或密码错误」且停留在登录页。

---

## 5. 执行与报告生成指引

**给 AI 的执行指令（可直接复用）**：

> 请按 `docs/API_TEST.md` 中的用例顺序逐条执行测试。
> 每条用例记录：实际执行的命令、实际输出、与预期是否一致。
> 全部执行完毕后，按文末「测试报告模版」生成 `docs/TEST_REPORT.md`。
> 若某条用例因环境缺失（如 Docker 未安装）无法执行，必须在报告中明确标注为「未执行」并说明原因，**不得臆造通过结果**。

---

## 6. 测试报告模版

```markdown
# 测试报告：医生数据迁移至 MySQL

## 1. 基本信息
| 项目 | 内容 |
| --- | --- |
| 测试日期 | <YYYY-MM-DD HH:mm> |
| 测试人员 | <姓名 / AI 模型> |
| 被测版本 | <commit hash 或分支名> |
| 测试环境 | 操作系统 / JDK 版本 / MySQL 版本 / Docker 版本 |
| 测试依据 | docs/API_TEST.md |

## 2. 结果汇总
| 总数 | 通过 | 失败 | 未执行 |
| --- | --- | --- | --- |
| 25 | x | x | x |

## 3. 用例明细
| 编号 | 用例名称 | 预期 | 实际 | 结论 |
| --- | --- | --- | --- | --- |
| TC-01 | 数据库容器健康检查 | ... | ... | 通过/失败/未执行 |

## 4. 缺陷列表
| 编号 | 关联用例 | 描述 | 严重级别 | 状态 |
| --- | --- | --- | --- | --- |

## 5. 结论
<是否满足验收标准，是否可以进入下一阶段>

## 6. 风险与说明
<未能覆盖的场景、环境限制等>
```

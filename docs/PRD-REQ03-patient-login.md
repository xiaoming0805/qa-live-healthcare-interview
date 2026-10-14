# 问诊用户用户名密码登录

## 0. 文档信息

| 项目 | 内容 |
| --- | --- |
| 文档名称 | 问诊用户用户名密码登录 |
| 文档编号 | PRD-REQ03 |
| 关联需求 | 面试需求列表「需求3」 |
| 版本 | v1.0 |
| 状态 | 已确认 |
| 创建日期 | 2026-10-14 |
| 最后更新 | 2026-10-14 |
| 评审人 | 待评审 |

> 本文档基于 `docs/PRD_TEMPLATE.md` 模版编写。

### 变更记录

| 版本 | 日期 | 变更人 | 变更内容 |
| --- | --- | --- | --- |
| v1.0 | 2026-10-14 | `<面试者姓名>` | 初稿 |

---

## 1. 背景与目标

### 1.1 业务背景
当前问诊用户（患者）进入问诊页时，采用「姓名 + 生日」的身份验证方式：用户在 `Consultation.vue` 中输入姓名与生日，系统在**前端内存**中查找匹配记录，找不到就以 `patient${Date.now()}` 为 id 自动创建一个账户。

### 1.2 问题陈述
1. **不是真正的认证**：只要知道某人的姓名和生日（通常是公开信息）就能冒用其身份，无法作为登录凭据。
2. **账户只在内存中**：页面刷新后新建的账户立即消失，「再次输入相同信息即可登录」的承诺实际上不成立。
3. **无凭据概念**：系统没有用户名与密码，用户无法主动保护自己的问诊记录。
4. **前端持有全部患者数据**：`patient-user.json` 中 5 条患者记录（含姓名、生日、手机号）可被任意浏览器下载。

### 1.3 目标（Goals）
- G1：为问诊用户提供**用户名 + 密码**的登录方式。
- G2：患者凭据由服务端校验，患者数据持久化到 MySQL。
- G3：支持新用户注册，替代原先「首次输入自动建号」的隐式逻辑。
- G4：接口响应不包含 `password` 字段。

### 1.4 非目标（Non-Goals）
- 不引入 Spring Security、JWT、Session 等完整认证体系，登录成功后前端仅保留当前用户对象（与既有医生登录保持一致）。
- 不做「找回密码」「修改密码」「手机号验证」等能力。
- 不迁移问题数据（`question-list.json`），问诊记录本次仍为前端内存态。
- 不改动首页 i18n（需求1）与医生模块（需求2）。

### 1.5 成功度量

| 指标 | 当前值 | 目标值 | 验证方式 |
| --- | --- | --- | --- |
| 登录凭据 | 姓名 + 生日（前端比对） | 用户名 + 密码（服务端校验） | 凭据错误返回 401 |
| 账户持久性 | 内存态，刷新即失效 | MySQL `patient` 表 | 重启后端后仍可登录 |
| 密码暴露 | 前端 JSON 含姓名/生日 | 接口不下发 `password` | `curl` 响应体无 `password` |
| 注册能力 | 隐式自动建号 | 显式注册接口 | 注册后可用新账号登录 |

---

## 2. 术语表

| 术语 | 说明 |
| --- | --- |
| 问诊用户 / 患者 | 发起咨询的一方，本特性中与「患者」同义 |
| 注册 | 用户主动创建账号，需提供用户名、密码、姓名、生日 |
| 登录态 | 前端 `store.currentPatient`，仅存活于当前会话 |

---

## 3. 用户角色与使用场景

### 3.1 角色

| 角色 | 说明 | 关注点 |
| --- | --- | --- |
| 新用户 | 首次使用平台的问诊用户 | 能否快速注册并开始问诊 |
| 老用户 | 已有账号的问诊用户 | 能否用用户名密码登录并看到自己的历史提问 |

### 3.2 核心场景（User Story）

- US-1：作为新用户，我希望注册一个账号，以便拥有可重复登录的身份。
- US-2：作为老用户，我希望用用户名和密码登录，以便查看我提交过的问题。
- US-3：作为用户，我希望密码错误时得到明确但不泄露账号是否存在的提示。
- US-4：作为用户，我希望登录后能切换到其他账号，以便家人共用一台设备。

---

## 4. 功能需求

### FR-1 患者表设计与种子数据迁移
**描述**：在 `qa_healthcare` 库创建 `patient` 表，存储用户名、密码与基本信息；将 `patient-user.json` 的 5 条数据迁移入库，并补充登录凭据。

**验收标准**：
- [ ] AC-1.1：`patient` 表含 `id/username/password/name/birthday/phone/gender` 等字段，`username` 唯一。
- [ ] AC-1.2：迁移后共 5 行，用户名分别为 `patient001` ~ `patient005`，初始密码统一为 `123456`。
- [ ] AC-1.3：`name`/`birthday`/`phone`/`gender` 与原 JSON 一致。
- [ ] AC-1.4：外键关联的既有 `patientId`（如问题数据中的 `patient001`）继续成立。

**优先级**：P0

### FR-2 患者注册接口
**描述**：提供注册接口，用户提交用户名、密码、姓名、生日后创建账号。

**验收标准**：
- [ ] AC-2.1：`POST /api/patients/register` 合法请求返回新用户信息（不含 `password`）。
- [ ] AC-2.2：用户名已存在时返回 `409`，提示「用户名已存在」。
- [ ] AC-2.3：用户名为空、密码为空、姓名或生日缺失时返回 `400`。
- [ ] AC-2.4：注册成功后可使用该账号登录。

**优先级**：P0

### FR-3 患者登录接口
**描述**：提供登录接口，校验用户名与密码。

**验收标准**：
- [ ] AC-3.1：`POST /api/patients/login` 凭据正确时返回用户信息（不含 `password`）。
- [ ] AC-3.2：密码错误返回 `401`。
- [ ] AC-3.3：用户名不存在返回 `401`，且 `message` 与密码错误**完全一致**。
- [ ] AC-3.4：两次响应的 HTTP 状态码一致，避免账号枚举。

**优先级**：P0

### FR-4 前端登录/注册页改造
**描述**：将 `Consultation.vue` 的「姓名 + 生日」验证表单改造为「用户名 + 密码」的登录/注册表单。

**验收标准**：
- [ ] AC-4.1：未登录时展示登录表单，含用户名、密码两个字段与登录按钮。
- [ ] AC-4.2：可在「登录 / 注册」之间切换；注册表单额外要求姓名与生日。
- [ ] AC-4.3：登录成功进入问诊主界面，顶部显示当前用户名与「切换用户」按钮。
- [ ] AC-4.4：凭据错误时给出明确提示，不进入问诊界面。
- [ ] AC-4.5：登录失败时按钮 loading 状态可恢复，不出现卡死。

**优先级**：P0

### FR-5 登录态与会话
**描述**：登录成功后前端保持当前用户，供问诊页提交问题时携带。

**验收标准**：
- [ ] AC-5.1：`store.currentPatient` 在登录成功后被赋值。
- [ ] AC-5.2：点击「切换用户」清空登录态并回到登录表单。
- [ ] AC-5.3：登录态仅存活于当前会话，刷新页面需重新登录（与医生登录行为一致）。

**优先级**：P1

---

## 5. 非功能需求

| 编号 | 类别 | 要求 | 验证方式 |
| --- | --- | --- | --- |
| NFR-1 | 安全 | 注册/登录接口响应不得包含 `password` | 检查响应体 |
| NFR-2 | 安全 | 登录失败不区分「用户名不存在」与「密码错误」 | 对比两次响应 |
| NFR-3 | 规范 | 所有接口统一返回 `{ code, message, data }` | 检查响应体 |
| NFR-4 | 一致性 | 与需求2 的医生模块保持相同的分层与异常处理风格 | 代码走查 |
| NFR-5 | 数据 | 表结构由 `init/*.sql` 管理，`ddl-auto=validate` | 查看配置 |

> 说明：与需求2 一致，`password` 为明文存储，仅用于演示；生产环境应存 BCrypt 哈希。此风险记录于第 9 节。

---

## 6. 数据模型

### 6.1 表结构

```sql
CREATE TABLE IF NOT EXISTS patient (
    id         VARCHAR(32)  NOT NULL COMMENT '患者业务主键',
    username   VARCHAR(64)  NOT NULL COMMENT '登录用户名',
    password   VARCHAR(128) NOT NULL COMMENT '登录密码（演示用明文）',
    name       VARCHAR(64)  NOT NULL COMMENT '患者姓名',
    birthday   DATE                  COMMENT '生日',
    phone      VARCHAR(32)           COMMENT '联系电话',
    gender     VARCHAR(8)            COMMENT '性别',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_patient_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
```

**设计说明**：
- `id` 沿用 `patient001` 形式，使 `question-list.json` 中既有的 `patientId` 引用继续成立。
- `birthday` 使用 `DATE` 而非字符串：该字段在原 JSON 中即为 `YYYY-MM-DD` 格式，且在原逻辑里参与身份匹配，用日期类型更严谨。

### 6.2 数据迁移方案

| 来源 | 目标 | 转换规则 | 校验方式 |
| --- | --- | --- | --- |
| `patient-user.json[].id` | `patient.id` | 原值 | 比对 id 集合 |
| —— | `patient.username` | 按 id 生成 `patient001..005` | 5 行均非空 |
| —— | `patient.password` | 统一初始化为 `123456` | 可用该密码登录 |
| `patient-user.json[].birthday` | `patient.birthday` | `YYYY-MM-DD` 字符串 → `DATE` | 逐条比对 |
| 其余字段 | 同名列 | 原值 | 逐字段比对 |

---

## 7. 接口设计

统一前缀 `/api/patients`，统一响应包装 `{ "code": 0, "message": "success", "data": <T> }`。

### 7.1 `POST /api/patients/register`

**用途**：患者注册

**请求体**：
| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 登录用户名，唯一 |
| `password` | string | 是 | 登录密码 |
| `name` | string | 是 | 姓名 |
| `birthday` | string | 是 | 生日，`YYYY-MM-DD` |

**成功响应**（200）：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "patient006",
    "username": "alice",
    "name": "张三",
    "birthday": "1990-05-20",
    "phone": null,
    "gender": null
  }
}
```

**异常响应**：
| HTTP | code | message | 场景 |
| --- | --- | --- | --- |
| 400 | 400 | 用户名不能为空 / 密码不能为空 / 姓名不能为空 / 生日格式不正确 | 参数校验失败 |
| 409 | 409 | 用户名已存在 | 用户名冲突 |

### 7.2 `POST /api/patients/login`

**用途**：患者登录

**请求体**：
```json
{ "username": "patient001", "password": "123456" }
```

**成功响应**（200）：`data` 为患者对象（不含 `password`）

**异常响应**：
| HTTP | code | message | 场景 |
| --- | --- | --- | --- |
| 401 | 401 | 用户名或密码错误 | 用户名不存在**或**密码错误 |

### 7.3 `GET /api/patients/{id}`

**用途**：按 id 查询患者（供调试与后续扩展）

**异常响应**：
| HTTP | code | message | 场景 |
| --- | --- | --- | --- |
| 404 | 404 | 患者不存在: `{id}` | id 不存在 |

---

## 8. 交互与 UI 说明

| 页面 | 改动 | 加载态 | 错误态 |
| --- | --- | --- | --- |
| `Consultation.vue` 未登录 | 「姓名 + 生日」表单 → 「用户名 + 密码」登录表单，可切换到注册 | 提交按钮 loading | message 提示具体原因 |
| `Consultation.vue` 已登录 | 顶部显示用户名与「切换用户」 | — | — |

**交互细节**：
- 默认展示**登录**表单（老用户是高频场景）；注册通过表单下方的「没有账号？立即注册」链接切换。
- 注册表单字段：用户名、密码、姓名、生日（生日沿用 `a-date-picker`）。
- 原先页面底部的蓝色提示「输入任意姓名和生日即可使用…」需替换为与账号体系一致的说明。

---

## 9. 边界条件与异常处理

| 场景 | 预期行为 |
| --- | --- |
| 用户名已存在（注册） | 返回 409，提示「用户名已存在」 |
| 用户名/密码为空 | 返回 400，提示对应字段 |
| 生日格式非法 | 返回 400，提示「生日格式不正确」 |
| 密码错误 / 用户名不存在 | 均返回 401，message 一致 |
| 数据库未启动 | 后端启动失败（`ddl-auto=validate`），前端提示请求异常 |
| 注册后立即可登录 | 因为写库，无内存态丢失问题 |

**已知风险**：
- `password` 明文存储与传输，仅适用于演示环境，生产需改为 BCrypt + HTTPS。
- 登录态仅存于前端内存，刷新即失效；生产应引入 Token 或 Session。

---

## 10. 依赖与约束

- **技术依赖**：Spring Boot 3.5.7、Spring Data JPA、MySQL 8.0、Vue 3 + vue-router
- **环境依赖**：JDK 17+、Docker（MySQL）、Node 18+
- **前置依赖**：需求2 已完成（MySQL 容器、JPA 依赖、统一响应与异常处理、前端 API 层）
- **约束**：全部代码须通过 AI 提示词生成

---

## 11. 任务拆分（Breakdown）

> 按序执行，每个任务可独立验证。

| 序号 | 任务 | 产出物 | 依赖 | 验证方式 |
| --- | --- | --- | --- | --- |
| T1 | 建表与种子数据脚本 | `init/03-patient-schema.sql`、`init/04-patient-seed.sql` | 需求2 的 T1 | phpMyAdmin 中 `patient` 表有 5 行 |
| T2 | 患者实体与仓储 | `entity/Patient.java`、`repository/PatientRepository.java` | T1 | 启动后 `ddl-auto=validate` 通过 |
| T3 | 患者 DTO 与请求对象 | `dto/PatientDTO.java`、`dto/PatientRegisterRequest.java`、`dto/PatientLoginRequest.java` | T2 | —— |
| T4 | 患者服务与控制器 | `service/PatientService.java`、`controller/PatientController.java` | T3 | `curl` 注册/登录符合预期 |
| T5 | 补充业务异常与全局处理 | `exception/PatientNotFoundException.java`、`exception/UsernameAlreadyExistsException.java`、扩展 `GlobalExceptionHandler` | T4 | 409/400/401 返回统一结构 |
| T6 | 前端 API 层扩展 | `src/api/patient.ts` | T4 | Network 中 `/api/patients/login` 返回 200 |
| T7 | 前端 store 改造 | `store/index.ts` 新增 `loginPatient` / `registerPatient`，移除 `verifyPatient` 的自动建号逻辑 | T6 | 登录后 `currentPatient` 被赋值 |
| T8 | 登录页改造 | `Consultation.vue` 登录/注册表单 | T7 | UI 可用，错误提示正确 |
| T9 | 补充接口测试文档 | `API_TEST.md` 追加 TC-16 ~ TC-25 | T4 | 用例可执行 |

---

## 12. 测试要点

- 注册：成功、用户名重复（409）、必填缺失（400）、生日格式非法（400）
- 登录：成功、密码错误（401）、用户名不存在（401，message 与密码错误一致）
- 注册后立即登录（验证写库生效）
- 响应体不得包含 `password`
- 前端：登录失败不进入问诊页、按钮 loading 恢复、「切换用户」清空登录态

详见 `API_TEST.md`（TC-16 ~ TC-25）。

---

## 13. 上线与回滚

**上线步骤**：
1. `docker compose up -d`（若容器已存在，需重建以执行新增的 `init/03`、`init/04` 脚本）
2. 重启 `qa-service-user`
3. 发布前端

**回滚方案**：
- 前端：恢复 `Consultation.vue` 的姓名+生日验证版本
- 数据：`DROP TABLE patient`，不影响医生模块

> ⚠️ 注意：`init/*.sql` 仅在 MySQL **数据卷为空**时执行。若容器已初始化过，新增脚本不会自动运行，需手动执行或 `docker compose down -v` 后重建。此项已在 `API_TEST.md` 的前置条件中说明。

---

## 14. 附录

- PRD 模版：`PRD_TEMPLATE.md`
- 提示词记录：`prompts.md`
- 接口测试：`API_TEST.md`
- 数据来源：`web/qa-web/src/data/patient-user.json`

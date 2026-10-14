# 医生数据迁移至 MySQL 并提供医生列表 API

## 0. 文档信息

| 项目 | 内容 |
| --- | --- |
| 文档名称 | 医生数据迁移至 MySQL 并提供医生列表 API |
| 文档编号 | PRD-REQ02 |
| 关联需求 | 面试需求列表「需求2」 |
| 版本 | v1.0 |
| 状态 | 已实现 |
| 作者 | `<面试者姓名>` |
| 创建日期 | 2026-10-14 |
| 最后更新 | 2026-10-14 |
| 评审人 | 待评审 |

### 变更记录

| 版本 | 日期 | 变更人 | 变更内容 |
| --- | --- | --- | --- |
| v1.0 | 2026-10-14 | `` | 初稿，含实现 |

---

## 1. 背景与目标

### 1.1 业务背景
当前 QA Live Healthcare 平台的医生、患者、问题三类数据全部以静态 JSON 文件的形式存放在前端工程 `web/qa-web/src/data/` 中，由 `src/store/index.ts` 直接 import 后放入内存态。这种方案在演示阶段可用，但不具备生产可用性。

### 1.2 问题陈述
1. **数据不可持久化**：`store` 是内存态，页面刷新后新增内容即丢失。
2. **修改需重新发布前端**：新增一名医生必须改动 `doctor-user-list.json` 并重新构建、发布前端工程。
3. **密码暴露在前端**：`doctor-user-list.json` 中存储了明文密码，任何访问者都能通过浏览器下载到全部医生凭据。
4. **无服务端能力**：`qa-service-user` 目前只有一个 `/api/test/cors` 测试接口，没有任何业务接口。

### 1.3 目标（Goals）
- G1：医生数据由 MySQL 存储，通过 `docker-compose` 一键创建数据库与可视化管理界面。
- G2：`qa-service-user` 提供支撑 `Doctors.vue` 页面展示医生列表的 API。
- G3：完成前后端联调，前端医生数据来自后端接口而非本地 JSON。
- G4：接口响应中不包含 `password` 字段。

### 1.4 非目标（Non-Goals）
- 不迁移患者数据（`patient-user.json`）与问题数据（`question-list.json`），本次仅处理「医生相关的数据」。
- 不引入 Spring Security / JWT 等认证授权体系，登录接口仅做最小可用的凭据校验。
- 不处理首页 i18n（需求1）与患者登录（需求3）。

### 1.5 成功度量

| 指标 | 当前值 | 目标值 | 验证方式 |
| --- | --- | --- | --- |
| 医生数据存储位置 | 前端 JSON | MySQL `doctor` 表 | phpMyAdmin 中可见 5 行 |
| 医生来源 | `import doctor-user-list.json` | `GET /api/doctors` | 断网/改库数据后前端随之变化 |
| 接口是否下发密码 | 是（前端 JSON 含明文密码） | 否 | `curl` 响应体中无 `password` 字段 |
| 启动方式 | 无 | `docker compose up -d` | 容器健康状态为 `healthy` |

---

## 2. 术语表

| 术语 | 说明 |
| --- | --- |
| 诊室开放 | 医生 `is_active = 1` 的状态，只有开放诊室的医生可被患者咨询 |
| 业务主键 | 沿用前端既有 `doc001` 形式的医生标识，用于与问题数据中的 `doctorId` 保持引用一致 |

---

## 3. 用户角色与使用场景

### 3.1 角色

| 角色 | 说明 | 关注点 |
| --- | --- | --- |
| 患者 | 浏览医生列表并选择医生问诊 | 能看到医生列表，仅开放诊室的医生可进入 |
| 医生 | 登录后进入自己的诊室 | 登录凭据由后端校验 |
| 运维/开发 | 维护医生数据 | 无需改前端代码即可增删医生 |

### 3.2 核心场景（User Story）

- US-1：作为患者，我希望在「医生团队」页看到全部医生，以便选择适合的医生咨询。
- US-2：作为患者，我希望首页「开放诊室」只展示在线医生，以便避免进入无法响应的诊室。
- US-3：作为运维，我希望直接改数据库就能上架/下架医生，以便无需重新发布前端。
- US-4：作为医生，我希望用用户名密码登录，以便进入自己的诊室处理患者问题。

---

## 4. 功能需求

### FR-1 容器化的 MySQL 与 phpMyAdmin
**描述**：提供 `docker-compose.yml`，一条命令拉起 MySQL 8.0 与 phpMyAdmin，并自动完成建库、建表与种子数据灌入。

**验收标准**：
- [ ] AC-1.1：`docker compose up -d` 后，`mysql` 与 `phpmyadmin` 两个容器均处于运行状态。
- [ ] AC-1.2：MySQL 使用 `utf8mb4` 字符集，中文医生姓名、科室无乱码。
- [ ] AC-1.3：访问 `http://localhost:8082` 可打开 phpMyAdmin 并登录。
- [ ] AC-1.4：宿主机 3306、8082 端口分别映射到 MySQL 与 phpMyAdmin，与既有 8080/8081 服务不冲突。

**优先级**：P0

### FR-2 医生表结构设计与数据迁移
**描述**：在 `qa_healthcare` 库中创建 `doctor` 表，并将 `doctor-user-list.json` 的 5 条数据迁移入库。

**验收标准**：
- [ ] AC-2.1：`doctor` 表存在，含 `id/username/password/name/title/department/avatar/experience/specialties/is_active` 等字段。
- [ ] AC-2.2：`specialties` 以 JSON 类型存储，读取后仍是数组。
- [ ] AC-2.3：迁移后共 5 行，`doc004`（刘敏医生）`is_active = 0`，其余为 1。
- [ ] AC-2.4：`username` 有唯一约束。

**优先级**：P0

### FR-3 医生列表查询 API
**描述**：提供接口返回医生列表，支持仅查询开放诊室的医生。

**验收标准**：
- [ ] AC-3.1：`GET /api/doctors` 返回全部 5 名医生。
- [ ] AC-3.2：`GET /api/doctors?active=true` 返回 4 名医生（不含 `doc004`）。
- [ ] AC-3.3：响应中**不含** `password` 字段。
- [ ] AC-3.4：`specialties` 字段为数组类型。

**优先级**：P0

### FR-4 医生详情查询 API
**描述**：按业务主键 id 或用户名查询单个医生，供问诊页按路由参数定位医生。

**验收标准**：
- [ ] AC-4.1：`GET /api/doctors/{id}` 返回对应医生，不存在时返回 404。
- [ ] AC-4.2：`GET /api/doctors/username/{username}` 返回对应医生，不存在时返回 404。

**优先级**：P1

### FR-5 医生登录 API
**描述**：将原前端内存中的密码比对迁移到服务端，避免因密码字段不再下发而导致医生登录功能失效。

**验收标准**：
- [ ] AC-5.1：`POST /api/doctors/login` 凭据正确时返回医生信息。
- [ ] AC-5.2：凭据错误时返回 401。
- [ ] AC-5.3：除登录接口外，其余接口响应均不含 `password`。

**优先级**：P1（回归修复）

### FR-6 前端改造与联调
**描述**：前端医生数据来源由本地 JSON 改为后端接口，并处理异步加载带来的加载态。

**验收标准**：
- [ ] AC-6.1：`store` 不再 `import` `doctor-user-list.json`。
- [ ] AC-6.2：应用启动时加载一次医生数据，各页面共享同一份。
- [ ] AC-6.3：`Doctors.vue` 在加载中显示 loading，无数据时显示空态。
- [ ] AC-6.4：修改数据库中 `doc004` 的 `is_active` 为 1 后刷新页面，「开放诊室」随之多出一位医生。

**优先级**：P0

---

## 5. 非功能需求

| 编号 | 类别 | 要求 | 验证方式 |
| --- | --- | --- | --- |
| NFR-1 | 安全 | 医生列表/详情接口响应不得包含 `password` | 检查接口响应体 |
| NFR-2 | 安全 | 登录失败不区分「用户名不存在」与「密码错误」 | 两种情况下返回相同 message |
| NFR-3 | 一致性 | 表结构由 SQL 脚本管理，`ddl-auto=validate`，禁止 Hibernate 自动改表 | 查看 `application.properties` |
| NFR-4 | 规范 | 所有接口统一返回 `{ code, message, data }` | 检查接口响应体 |
| NFR-5 | 可维护 | 前端请求走 vite proxy，代码中使用相对路径 `/api/**` | 查看 `vite.config.ts` |
| NFR-6 | 环境 | JDK 17+、MySQL 8.0、Docker | `java -version` |

> 说明：`password` 为明文存储，仅用于演示；生产环境应存 BCrypt 哈希，此项已作为已知风险记录于第 9 节。

---

## 6. 数据模型

### 6.1 表结构

```sql
CREATE TABLE IF NOT EXISTS doctor (
    id          VARCHAR(32)  NOT NULL COMMENT '医生业务主键，沿用前端既有 doc001 形式',
    username    VARCHAR(64)  NOT NULL COMMENT '登录用户名',
    password    VARCHAR(128) NOT NULL COMMENT '登录密码（演示用明文）',
    name        VARCHAR(64)  NOT NULL COMMENT '医生姓名',
    title       VARCHAR(64)           COMMENT '职称',
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
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
```

**设计说明**：
- `id` 沿用原 JSON 的业务主键而非自增，使 `question-list.json` 中已有的 `doctorId` 引用继续成立，迁移零转换。
- `specialties` 使用 JSON 列而非子表或逗号分隔：比子表轻量，比逗号分隔规范。JPA 侧通过 `StringListConverter`（`AttributeConverter`）与 `List<String>` 互转。
- `created_at` / `updated_at` 由数据库默认值维护，实体侧标记 `insertable = false, updatable = false`。

### 6.2 数据迁移方案

| 来源 | 目标 | 转换规则 | 校验方式 |
| --- | --- | --- | --- |
| `doctor-user-list.json[].id` | `doctor.id` | 原值 | 比对 id 集合 |
| `doctor-user-list.json[].specialties` | `doctor.specialties` | 数组 → JSON 文本 | 接口返回应为数组 |
| `doctor-user-list.json[].isActive` | `doctor.is_active` | `true/false` → `1/0` | 统计 `is_active=1` 应为 4 行 |
| 其余字段 | 同名列 | 原值 | 逐字段比对 |

---

## 7. 接口设计

统一前缀 `/api/doctors`，统一响应包装 `{ "code": 0, "message": "success", "data": <T> }`。

### 7.1 `GET /api/doctors`

**用途**：获取医生列表（供 `Doctors.vue` 与 `Home.vue`）

**请求参数**：
| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `active` | query | boolean | 否 | 传 `true` 仅返回开放诊室的医生 |

**成功响应**（200）：
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": "doc001",
      "username": "dr-zhang-wei",
      "name": "张伟医生",
      "title": "主任医师",
      "department": "心内科",
      "avatar": "https://...",
      "experience": "15年临床经验",
      "specialties": ["高血压", "冠心病", "心律失常"],
      "isActive": true
    }
  ]
}
```

### 7.2 `GET /api/doctors/{id}`

**用途**：按业务主键查询医生

**异常响应**：
| HTTP | code | message | 场景 |
| --- | --- | --- | --- |
| 404 | 404 | 医生不存在: `{id}` | id 不存在 |

### 7.3 `GET /api/doctors/username/{username}`

**用途**：按用户名查询医生（问诊页路由参数）

**异常响应**：
| HTTP | code | message | 场景 |
| --- | --- | --- | --- |
| 404 | 404 | 医生不存在: `{username}` | username 不存在 |

### 7.4 `POST /api/doctors/login`

**用途**：医生登录

**请求体**：
```json
{ "username": "dr-zhang-wei", "password": "123456" }
```

**成功响应**（200）：`data` 为医生对象（不含 `password`）

**异常响应**：
| HTTP | code | message | 场景 |
| --- | --- | --- | --- |
| 401 | 401 | 用户名或密码错误 | 用户名不存在**或**密码错误 |

---

## 8. 交互与 UI 说明

| 页面 | 改动 | 加载态 | 空态 | 错误态 |
| --- | --- | --- | --- | --- |
| `App.vue` | 启动时调用 `store.loadDoctors()` | — | — | 顶部 message 提示加载失败 |
| `Home.vue` | 统计与「开放诊室」依赖后端数据 | `a-spin` 包裹相关区块 | — | 由 App.vue 统一提示 |
| `Doctors.vue` | 医生卡片来自后端 | `a-spin` 包裹卡片网格 | `a-empty`「暂无医生数据」 | 由 App.vue 统一提示 |
| `DoctorLogin.vue` | 登录改为调用后端接口 | 按钮 `loading` | — | message「用户名或密码错误」 |

---

## 9. 边界条件与异常处理

| 场景 | 预期行为 |
| --- | --- |
| 数据库未启动 / 连接失败 | 后端启动失败（`ddl-auto=validate`），前端加载失败并提示 |
| 医生不存在 | 返回 404 + 统一错误包装 |
| 登录密码错误 | 返回 401，提示语与「用户名不存在」一致，避免账号枚举 |
| `specialties` 为空 | 返回空数组 `[]`，前端不报错 |
| 前端直连后端（未走 proxy） | `CorsConfig` 已放开跨域，仍可访问 |

**已知风险**：
- `password` 明文存储与明文传输，仅适用于演示环境，生产需改为 BCrypt + HTTPS。

---

## 10. 依赖与约束

- **技术依赖**：Spring Boot 3.5.7、Spring Data JPA、MySQL Connector/J、MySQL 8.0、Vue 3 + Vite 5
- **环境依赖**：JDK 17+、Maven 3.8+、Docker / Docker Compose
- **约束**：全部代码须通过 AI 提示词生成，可人工微调

---

## 11. 任务拆分（Breakdown）

> 供 AI 按序执行，每个任务可独立验证。

| 序号 | 任务 | 产出物 | 依赖 | 验证方式 |
| --- | --- | --- | --- | --- |
| T1 | 编写容器编排与初始化脚本 | `docker-compose.yml`、`init/01-schema.sql`、`init/02-seed.sql` | - | `docker compose up -d` 后 phpMyAdmin 可见 5 行 |
| T2 | 后端引入持久层依赖与数据源配置 | `pom.xml`、`application.properties` | T1 | `mvn compile` 通过 |
| T3 | 编写实体与 JSON 转换器 | `Doctor.java`、`StringListConverter.java` | T2 | 启动后 `specialties` 可正确读出 |
| T4 | 编写仓储、DTO、服务、控制器 | `DoctorRepository`、`DoctorDTO`、`DoctorService`、`DoctorController` | T3 | `curl` 各接口返回符合预期 |
| T5 | 编写统一响应与全局异常处理 | `ApiResponse`、`GlobalExceptionHandler`、两个异常类 | T4 | 404/401 返回统一结构 |
| T6 | 前端引入 API 层与代理 | `types/index.ts`、`api/http.ts`、`api/doctor.ts`、`vite.config.ts` | T4 | 浏览器 Network 中 `/api/doctors` 返回 200 |
| T7 | 前端 store 改造为异步加载 | `store/index.ts` | T6 | 页面数据来自接口 |
| T8 | 页面适配加载态 | `App.vue`、`Home.vue`、`Doctors.vue`、`DoctorLogin.vue` | T7 | UI 无空白闪烁、无报错 |
| T9 | 编写接口测试文档并执行测试 | `API_TEST.md`、测试报告 | T4 | 测试用例全部通过 |

---

## 12. 测试要点

- 列表接口的「全量」与「仅在线」两种模式
- 接口响应**不得**包含 `password`（安全回归点）
- `specialties` 的 JSON 往返正确性（中文、数组）
- 404 / 401 的错误包装结构一致性
- 端到端：改库 → 刷新页面 → UI 变化（证明数据真的来自数据库）

详见 `API_TEST.md`。

---

## 13. 上线与回滚

**上线步骤**：
1. `docker compose up -d` 创建数据库并灌入种子数据
2. 启动 `qa-service-user`（8080）
3. 构建并发布前端

**回滚方案**：
- 前端：回退到 `store` 读取本地 JSON 的版本（`doctor-user-list.json` 保留未删除，可直接恢复）
- 数据：`docker compose down -v` 清空数据卷后按 `init/*.sql` 重建

---

## 14. 附录

- 提示词记录：`prompts.md`
- 接口测试：`API_TEST.md`
- 数据来源：`web/qa-web/src/data/doctor-user-list.json`

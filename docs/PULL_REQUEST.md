# Pull Request 描述

> 本文件用于撰写 GitHub PR。**标题**填在 PR 标题栏，**正文**复制到 PR 描述框。
> 内容随代码提交一并放在仓库中，便于评审对照。

---

## 标题

```
feat: 完成面试需求 —— 首页中英文切换、医生数据迁移至 MySQL、患者用户名密码登录
```

---

## 正文

### 背景

按仓库 README 的面试需求，完成三项特性的开发、测试与文档。所有代码均通过 AI 提示词生成，
完整过程记录见 `docs/prompts.md`。

### 改动内容

#### 需求1：首页中英文切换

- 引入 `vue-i18n`，语言资源存放于 `web/qa-web/src/locales/{zh-CN,en-US}.json`
- `AppHeader.vue` 右上角新增语言切换下拉，选择结果持久化到 `localStorage`，
  初始语言按浏览器语言推断
- `Home.vue` 全部 15 处文案接入 i18n
- 按需求「只需要处理首页本身」，其他页面文案保持不变

#### 需求2：医生数据迁移至 MySQL

- 新增 `docker-compose.yml`（MySQL 8.0 + phpMyAdmin），`init/01-schema.sql`、`init/02-seed.sql`
  负责建表与种子数据（5 条医生数据迁移，`specialties` 用 JSON 列存储）
- `qa-service-user` 补齐持久层：Spring Data JPA + MySQL 驱动、`Doctor` 实体、
  `StringListConverter`（JSON 列 ⇄ `List<String>` 互转）
- 新增 4 个查询接口：医生列表（支持 `?active=true`）、按 id 查、按用户名查、登录
- 统一响应包装 `{code, message, data}` 与全局异常处理
- 前端新增 `api/` 层，`store` 由本地 JSON 改为异步请求，`vite` 配置 `/api` 代理
- **安全**：接口响应不含 `password` 字段

#### 需求3：问诊用户用户名密码登录

- 新增 `patient` 表与 5 条种子数据（`init/03`、`init/04`）
- `POST /api/patients/register`、`POST /api/patients/login`、`GET /api/patients/{id}`
- `Consultation.vue` 由「姓名 + 生日」验证改为「用户名 + 密码」登录，支持切换注册模式
- 登录失败不区分「用户名不存在」与「密码错误」，避免账号枚举

### 文档

| 文档 | 说明 |
| --- | --- |
| `docs/PRD_TEMPLATE.md` | 通用 PRD 模版 |
| `docs/PRD-REQ02-doctor-data-to-mysql.md` | 需求2 的 PRD |
| `docs/PRD-REQ03-patient-login.md` | 需求3 的 PRD（含 T1~T9 任务拆分） |
| `docs/API_TEST.md` | 25 条接口测试用例（TC-01 ~ TC-25） |
| `docs/TEST_REPORT.md` | 测试报告 |
| `docs/prompts.md` | 全过程提示词记录 |

### 如何验证

```bash
# 1) 启动数据库（或使用既有的 MySQL 实例，见 application.properties 注释）
docker compose up -d

# 2) 启动后端（注意：需 JDK 17+，本项目以 JDK 21 验证）
cd server/qa-service-user && ./mvnw spring-boot:run     # 8080

# 3) 启动前端
cd web/qa-web && npm install && npm run dev             # 5173
```

冒烟检查：

```bash
# 医生接口不应下发密码
curl -s http://localhost:8080/api/doctors | grep -c '"password"'    # 期望 0

# 患者登录
curl -s -X POST http://localhost:8080/api/patients/login \
  -H "Content-Type: application/json" \
  -d '{"username":"patient001","password":"123456"}'                # 期望 200，id=patient001
```

完整用例见 `docs/API_TEST.md`。

### 测试结果

| 测试类别 | 用例数 | 通过 | 失败 | 未执行 |
| --- | --- | --- | --- | --- |
| 后端接口 | 25 | 24 | 0 | 1 |
| 前端 UI（Playwright 驱动真实浏览器） | 13 | 13 | 0 | 0 |
| **合计** | **38** | **37** | **0** | **1** |

- 未执行的 1 条为 TC-01（数据库容器健康检查），验证环境未使用 Docker
- 端到端验证通过：修改数据库中 `doc004` 的 `is_active` 后，接口与页面展示随之变化，
  证明数据确实来自 MySQL
- 详见 `docs/TEST_REPORT.md`

### 已知限制

- 数据库中口令为明文，前端演示账号亦为明文，仅适用于演示环境；生产需改为 BCrypt + HTTPS
- 登录态仅存于前端内存，刷新页面需重新登录（与既有医生登录行为一致）
- `init/*.sql` 仅在 MySQL 数据卷为空时执行；若容器已初始化过，需 `docker compose down -v` 后重建
- 患者 id 生成采用「现有最大值 + 1」，高并发注册下存在冲突可能
- 未做跨浏览器验证，仅在 Chromium 上执行

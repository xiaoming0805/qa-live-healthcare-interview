# 提示词记录（prompts.md）

> 本文件记录完成面试需求过程中所使用的全部 AI 提示词。
> 格式：**目标 → 提示词原文 → 产出**。（本文件应持续补充，直至需求全部完成）

---

## 使用说明

建议按以下阶段组织：

| 阶段 | 说明 | 典型产出 |
| --- | --- | --- |
| A. 需求分析 | 让 AI 读懂现有代码与需求 | 代码走读结论、影响面清单 |
| B. 方案设计 | 确定技术选型与边界 | 设计方案、数据库设计 |
| C. 文档编写 | 先写 PRD 再写代码 | `PRD_TEMPLATE.md`、`PRD-*.md` |
| D. 编码实现 | 按 PRD 的拆分任务逐步生成 | 源代码 |
| E. 测试验证 | 生成并执行测试 | `API_TEST.md`、测试报告 |
| F. 修正与复查 | 处理缺陷与回归 | 修复提交 |

---

## A. 需求分析

### A-1 代码走读

**目标**：在动手前完整了解现有代码，明确改造影响面。

**提示词原文**：
> 整体读取下 server 和 web 的代码，看看这项目做了什么

**产出**：得到项目全貌——前端 Vue3 问诊平台（6 页面）、医生/患者/问题三类数据以 JSON 形式存在前端并置于内存态 store；后端为 `qa-service-user`(8080)、`qa-service-question`(8081) 两个 Spring Boot 空骨架，仅有一个 `/api/test/cors` 测试接口；`qa-service-statistic` 为空目录。

**关键结论**：
- 医生数据入口只有一个：`src/store/index.ts` 中 `import doctorData from '../data/doctor-user-list.json'`
- 医生列表被 4 个页面消费：`Home.vue`、`Doctors.vue`、`Consultation.vue`、`DoctorRoom.vue`
- 医生登录依赖前端内存中的 `password` 比对

### A-2 影响面澄清

**提示词原文**：
> 根据readme.md的要求一步步讨论方案，
> 可以（继续深入 qa-service-user 加医生 API 的设计）

**产出**：确认改造边界——只迁移「医生相关数据」，患者与问题数据仍留前端 JSON；并识别出一个回归风险：若接口不下发 `password`，`store.loginDoctor()` 的前端比对会失效，必须补登录接口。

---

## B. 方案设计

### B-1 设计医生列表 API

**目标**：确定数据库设计、后端分层、接口契约与前端改造点。

**提示词原文**：
> 需要我深入某个模块（比如 qa-service-user 加医生 API 该怎么设计）吗？ → 可以

**产出**：
- `doctor` 表设计（`specialties` 用 JSON 列，`id` 沿用业务主键）
- 后端分层：`entity / repository / dto / service / controller` + 全局异常处理
- 接口契约：4 个查询接口 + 1 个登录接口
- 前端改造：抽 `types`、加 `api` 层、vite proxy、store 异步化

### B-2 选择交付范围

**提示词原文**：
> B（docker-compose + 建表脚本 + 后端接口 + 前端 store/API 改造，一次做完前后端联调）

**产出**：确定一次性打通端到端链路，而非分阶段交付。

---

## C. 文档编写

### C-1 编写 PRD 模版

**目标**：先产出可复用的 PRD 模版，再用模版驱动后续开发。

**提示词原文**：
> （由模版要求推导）在 /docs 目录中编写规范的 PRD 文档模版

**产出**：`docs/PRD_TEMPLATE.md`，含背景与目标、功能/非功能需求、数据模型、接口设计、边界条件、任务拆分等 14 个章节。

### C-2 用模版编写本特性 PRD

**提示词原文**：
> 指导 AI 使用以上模版编写此特性的 PRD 文档

**产出**：`docs/PRD-REQ02-doctor-data-to-mysql.md`，6 条功能需求（FR-1 ~ FR-6）、6 条非功能需求、5 个接口契约，以及 T1 ~ T9 的任务拆分表。

---

## D. 编码实现

### D-1 容器编排与初始化脚本

**提示词原文**：
> 按 PRD 的 T1 任务，创建 docker-compose.yml（MySQL + phpMyAdmin）与 init 下的建表、种子数据脚本

**产出**：`docker-compose.yml`、`init/01-schema.sql`、`init/02-seed.sql`。

### D-2 后端实现

**提示词原文**：
> 按 PRD 的 T2 ~ T5 任务实现 qa-service-user 的医生模块

**产出**：`pom.xml` 新增依赖、`application.properties` 数据源配置、`Doctor`、`StringListConverter`、`DoctorRepository`、`DoctorDTO`、`ApiResponse`、`LoginRequest`、`DoctorService`、`DoctorController`、`GlobalExceptionHandler` 及两个业务异常类。

### D-3 前端改造

**提示词原文**：
> 按 PRD 的 T6 ~ T8 任务完成前端 store 与 API 层改造

**产出**：`types/index.ts`、`api/http.ts`、`api/doctor.ts`、`vite.config.ts`（proxy）、`store/index.ts`（异步化）、`App.vue`/`Home.vue`/`Doctors.vue`/`DoctorLogin.vue`（加载态适配）。

---

## E. 测试验证

### E-1 编写接口测试文档

**提示词原文**：
> 使用 curl 构建对于以上需求中所涉及的所有接口的测试，并提供 docs/API_TEST.md

**产出**：`docs/API_TEST.md`，含 15 条用例（TC-01 ~ TC-15），覆盖容器健康、表结构、种子数据、4 个接口的正常与异常分支、密码泄露回归、账号枚举防护，以及端到端的数据来源验证。

### E-2 执行测试并生成报告

**提示词原文**：
> 请按 docs/API_TEST.md 中的用例顺序逐条执行测试，并按文末模版生成 docs/TEST_REPORT.md

**产出**：`docs/TEST_REPORT.md` —— **状态：已执行**。

> 执行结果：25 条用例中 **24 条通过、0 失败、1 条未执行**（TC-01，测试环境无 Docker）。
> 执行方式：以 JDK 21（`F:\Program Files\Java\jdk-21.0.12.1`）启动 `qa-service-user`，连接既有 MySQL 5.7.44（10.130.19.218）的 `qa_healthcare` 库；用例通过 Python 脚本调用 HTTP 接口逐一断言。
> 过程中的 1 处失败为**测试脚本自身缺陷**（变量赋值顺序写反），修复后全部通过，未发现产品缺陷。

---

## F. 修正与复查

> 待补充。

---

## G. 需求1：首页中英文切换

### G-1 需求确认与技术选型

**目标**：在首页实现中英文切换，语言资源存于 `src/locales`。

**提示词原文**：
> 接着开发

**产出**：
- **选型结论**：使用 `vue-i18n`（Vue 3 生态标准 i18n 方案），语言资源以 JSON 形式存放于 `web/qa-web/src/locales/`，满足需求中「语言资源文件需要保存在 `web/qa-web/src/locales` 目录」的约束。
- **范围界定**：严格按需求「只需要处理首页本身，无需处理其他页面」，仅 `Home.vue` 的文案参与翻译；`AppHeader.vue` 只新增语言切换控件，导航菜单文案保持中文不动。

### G-2 实现

**提示词原文**：
> 接着开发（按需求1实现首页 i18n）

**产出**：
- `src/locales/zh-CN.json`、`src/locales/en-US.json`：按 `home.hero` / `home.statistics` / `home.activeRooms` 分组组织
- `src/locales/index.ts`：`createI18n` 实例、`SUPPORTED_LOCALES`、`setLocale` / `getLocale`；语言选择持久化到 `localStorage`，并按浏览器语言推断初始值
- `src/main.ts`：注册 i18n 插件
- `src/components/AppHeader.vue`：右上角新增语言切换下拉（`a-select`）
- `src/views/Home.vue`：15 处文案改为 `t('...')` 调用
- `package.json`：新增 `vue-i18n` 依赖

---

## H. 需求3：问诊用户用户名密码登录

### H-1 用模版编写本特性 PRD

**目标**：按需求「指导 AI 使用以上模版编写此特性的 PRD 文档」执行。

**提示词原文**：
> 接着开发（按需求3先用 PRD_TEMPLATE.md 写 PRD）

**产出**：`docs/PRD-REQ03-patient-login.md`。
- 背景指出原「姓名 + 生日」验证的三个缺陷：生日属公开信息不构成凭据、账户只在内存、前端持有全部患者数据。
- 5 条功能需求（FR-1 ~ FR-5）、5 条非功能需求、3 个接口契约。
- 第 11 节的 T1 ~ T9 任务拆分，作为后续编码阶段的执行清单。

### H-2 按 PRD 拆分实现

**提示词原文**：
> 按 PRD-REQ03 的 T1 ~ T9 实现患者注册与登录

**产出**：
- **T1 数据层**：`init/03-patient-schema.sql`、`init/04-patient-seed.sql`（5 条患者数据迁移，补充 `username = id`、`password = 123456`）
- **T2 ~ T3 实体与 DTO**：`entity/Patient.java`、`repository/PatientRepository.java`、`dto/PatientDTO.java`、`dto/PatientRegisterRequest.java`、`dto/PatientLoginRequest.java`
- **T4 服务与接口**：`service/PatientService.java`（注册参数校验、`patientNNN` 主键生成、登录凭据校验）、`controller/PatientController.java`
- **T5 异常**：`exception/PatientNotFoundException.java`(404)、`UsernameAlreadyExistsException.java`(409)、`InvalidRequestException.java`(400)，并扩展 `GlobalExceptionHandler`
- **T6 ~ T7 前端数据层**：`src/api/patient.ts`；`store/index.ts` 新增 `loginPatient` / `registerPatient`，移除 `verifyPatient` 与其依赖的 `state.patients` 内存数组
- **T8 页面**：`Consultation.vue` 由「姓名 + 生日」验证改为「用户名 + 密码」登录表单，支持切换注册模式
- **T9 测试**：`API_TEST.md` 追加 TC-16 ~ TC-25

### H-3 关键决策记录

| 决策点 | 选择 | 理由 |
| --- | --- | --- |
| 患者数据存储 | 落 MySQL `patient` 表 | 与医生模块一致；否则登录无法跨会话生效 |
| 是否提供注册接口 | 提供 | 原「首次输入自动建号」是隐式注册，改为显式注册更安全 |
| 校验方式 | 手写参数校验 | 不引入 `spring-boot-starter-validation`，保持依赖精简 |
| 患者 id 生成 | `max(现有 id 数字部分) + 1` | 保持 `patient006` 形式；并发风险已在代码注释与 PRD 中标注 |

---

## 附：面试者补充区

> 在实际操作中若使用了本文件未记录的其他提示词（如对话中的追问、局部返工、手工调整说明），请在此处补全，保持记录完整。

# 学生考勤管理系统

## 开发者信息

- **姓名：** 张三
- **学号：** 11111111
- **课程：** JAVA EE 开发实践
- **任课教师邮箱：** zhouf_t@swufe.edu.cn
- **项目阶段：** 第十四周——项目完善与测试

## 1. 项目简介

本项目是一个基于 Spring Boot 的学生考勤管理系统，用于练习 Java Web 项目中的分层设计、数据库访问、用户认证、服务端页面渲染、REST API、文件上传和 Excel 数据导入。

系统同时提供 Thymeleaf 页面和 REST API，目前围绕用户、学生和考勤三个核心模块展开。第十四周的重点是对已有功能进行完整性检查，排查 Bug，补充测试，并为项目交付做好准备。

## 2. 项目现状分析

### 2.1 功能完成情况

> 状态说明：✅ 已实现；🟡 已有部分代码但仍需完善；⬜ 尚未实现。

| 模块 | 功能 | 状态 | 说明 |
| --- | --- | --- | --- |
| 用户认证 | 注册、登录 | ✅ | 同时提供页面表单和 REST API |
| 用户认证 | Spring Security 集成 | ✅ | 已使用 `PasswordEncoder` 进行 BCrypt 密码加密与校验 |
| 用户认证 | 基于角色的访问控制 | 🟡 | 用户数据包含角色字段，但当前安全配置仍允许所有请求访问 |
| 用户管理 | 用户信息增删改查 | ✅ | 使用 `JdbcTemplate` 和手写 SQL 实现 |
| 学生管理 | 学生信息新增、查询、编辑、删除 | ✅ | 同时提供 REST API 和 Thymeleaf 页面 |
| 学生管理 | 搜索、排序、批量删除 | ✅ | 支持按学号、姓名、班级搜索 |
| 学生管理 | 学生列表分页 | ⬜ | 当前页面列表一次性加载全部学生，尚未分页 |
| 学生管理 | Excel 批量导入 | ✅ | 支持 `.xlsx` 和 `.xls` 文件 |
| 考勤管理 | 考勤打卡 | ✅ | 可记录课程、学生、座位、时间和状态 |
| 考勤管理 | 考勤记录查询、筛选、分页 | ✅ | 支持按课程、状态和时间范围查询 |
| 考勤管理 | 出勤统计 | 🟡 | 已提供学生统计 Service 和 REST API，尚未实现统计展示页面 |
| 考勤管理 | Excel 批量导入 | ✅ | 支持批量导入考勤记录 |
| 课程管理 | 课程信息 CRUD 与查询 | 🟡 | 已实现实体、Service 和 REST API，尚未实现管理页面 |
| 请假管理 | 请假申请与审批 | 🟡 | 已实现申请、审批和查询 REST API，尚未实现页面与角色授权 |
| 数据管理 | Excel 数据导出 | ⬜ | 尚未实现 |
| 系统管理 | 操作日志、备份与恢复 | ⬜ | 尚未实现 |
| 项目质量 | 统一异常处理、性能优化、自动化测试 | 🟡 | 已有部分基础代码，仍需继续完善 |

### 2.2 待完善功能清单

- [ ] 完善基于 `ADMIN`、`TEACHER`、`STUDENT` 的角色访问控制
- [ ] 为学生列表增加分页功能
- [ ] 增加出勤统计展示页面
- [ ] 为请假申请与审批增加页面、角色授权和请假统计
- [ ] 为课程管理增加页面、学生课程查询和课程表展示
- [ ] 增加学生信息与考勤记录 Excel 导出功能
- [ ] 增加操作日志记录
- [ ] 增加数据备份与恢复方案
- [ ] 完善统一异常处理、参数校验和日志记录
- [ ] 增加单元测试、集成测试和大数据量性能测试

## 3. 技术栈

| 分类 | 技术 |
| --- | --- |
| 开发语言 | Java 17 |
| 核心框架 | Spring Boot |
| Web 开发 | Spring MVC、Spring Web |
| 页面模板 | Thymeleaf |
| 数据访问 | Spring Data JPA、JdbcTemplate |
| 数据库 | PostgreSQL |
| 安全组件 | Spring Security、BCrypt |
| Excel 处理 | Apache POI |
| 工具库 | Lombok |
| 项目构建 | Maven |
| 测试框架 | JUnit、Spring Boot Test |

## 4. 项目架构

项目采用分层架构，一个典型请求的调用链如下：

```text
浏览器 / HTTP Client
        ↓
Controller（接收请求、参数绑定、返回页面或 JSON）
        ↓
Service（业务逻辑、校验、查询条件组装、Excel 解析）
        ↓
Repository / Dao（JPA 或 JdbcTemplate 数据访问）
        ↓
PostgreSQL
```

项目中存在两种数据访问方式：

- `UserDao` 使用 `JdbcTemplate` 和手写 SQL 操作用户数据。
- `StudentRepository`、`AttendanceRepository` 使用 Spring Data JPA 操作学生和考勤数据。

REST API 使用统一的 `Result<T>` 返回格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

## 5. 工程结构

```text
attendance-system/
├── pom.xml                                # Maven 依赖与构建配置
├── sql/
│   ├── attendance.sql                     # 用户、课程、选课、考勤相关 SQL
│   └── student.sql                        # 学生示例数据 SQL
├── test/
│   ├── security-test.http                 # 注册与登录接口测试
│   ├── attendance-specification-test.http # 考勤条件查询测试
│   ├── test1.http                         # 用户接口测试
│   └── test2.http                         # 学生与考勤接口测试
└── src/
    ├── main/
    │   ├── java/com/example/attendance/
    │   │   ├── AttendanceSystemApplication.java # Spring Boot 启动入口
    │   │   ├── common/                    # 通用组件，如 Result<T>
    │   │   ├── config/                    # Security、静态资源、异常处理配置
    │   │   ├── controller/                # REST API 与页面控制器
    │   │   ├── dao/                       # JdbcTemplate 数据访问层
    │   │   ├── dto/                       # 请求、响应、查询、统计等 DTO
    │   │   ├── entity/                    # User、Student、Attendance 等实体
    │   │   ├── repository/                # Spring Data JPA Repository
    │   │   └── service/                   # Service 接口及 impl 实现
    │   └── resources/
    │       ├── application.properties     # 仓库中的非敏感基础配置
    │       ├── static/css/                 # 静态样式文件
    │       └── templates/                  # Thymeleaf 页面模板
    └── test/                               # Spring Boot 自动化测试
```

## 6. 本地运行

### 6.1 环境要求

- JDK 17
- PostgreSQL
- Maven，或项目自带的 Maven Wrapper
- IntelliJ IDEA、VS Code 等开发工具（可选）

### 6.2 初始化数据库

在 PostgreSQL 中创建项目使用的数据库，然后根据实际需要执行：

- `sql/attendance.sql`：用户、课程、选课和考勤相关表及示例数据
- `sql/student.sql`：学生示例数据

> `sql/attendance.sql` 包含 `DROP TABLE`、`CREATE DATABASE`、建表和初始化数据语句。执行前请确认当前数据库环境和已有数据，避免误删除。

### 6.3 完善本地配置

项目连接服务器数据库，真实数据库地址、账号和密码属于敏感信息，因此不会上传到代码仓库。仓库中的 `application.properties` 只保留非敏感基础配置；运行前请在本地补充数据源和上传目录配置。

```properties
spring.application.name=attendance-system
server.port=8085

# PostgreSQL 数据源，请替换为自己的环境信息
spring.datasource.url=jdbc:postgresql://<host>:<port>/<database>
spring.datasource.username=<username>
spring.datasource.password=<password>
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA 配置，可根据实际环境调整
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Excel 上传文件的临时保存目录
file.upload.path=<your-upload-directory>/

# 文件上传限制
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

请勿提交真实服务器地址、数据库账号、数据库密码或其他密钥。更安全的做法是通过环境变量、外部配置文件或密钥管理服务提供敏感配置。

### 6.4 启动项目

Linux / macOS：

```bash
./mvnw spring-boot:run
```

如果 Maven Wrapper 没有执行权限：

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Windows：

```bat
mvnw.cmd spring-boot:run
```

也可以直接运行 `AttendanceSystemApplication` 的 `main` 方法。

### 6.5 页面入口

项目默认端口为 `8085`：

| 页面 | 地址 |
| --- | --- |
| 登录页 | <http://localhost:8085/login> |
| 注册页 | <http://localhost:8085/register> |
| 系统首页 | <http://localhost:8085/dashboard> |
| 学生管理 | <http://localhost:8085/student/page/list> |
| 学生导入 | <http://localhost:8085/student/page/import> |
| 考勤打卡 | <http://localhost:8085/attendance/page/checkin> |
| 考勤记录 | <http://localhost:8085/attendance/page/list> |
| 考勤导入 | <http://localhost:8085/attendance/page/import> |

## 7. 主要 REST API

### 7.1 认证与用户接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/auth/register` | 注册用户 |
| POST | `/auth/login` | 用户登录 |
| POST | `/user/add` | 新增用户 |
| GET | `/user/{id}` | 按 ID 查询用户 |
| GET | `/user/username/{username}` | 按用户名查询用户 |
| GET | `/user/teachers` | 查询教师用户 |
| PUT | `/user/update` | 更新用户 |
| DELETE | `/user/{id}` | 删除用户 |

### 7.2 学生接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/student/create` | 新增学生 |
| GET | `/student/{studentId}` | 按学号查询学生 |
| GET | `/student/all` | 查询全部学生 |
| GET | `/student/list?className=...` | 按班级查询学生 |
| DELETE | `/student/{studentId}` | 删除学生 |

### 7.3 考勤接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/attendance/create` | 新增考勤记录 |
| GET | `/attendance/all` | 查询全部考勤记录 |
| GET | `/attendance/student/{studentId}` | 按学生 ID 查询考勤记录 |
| GET | `/attendance/course/{courseId}` | 按课程 ID 查询考勤记录 |
| GET | `/attendance/page` | 分页查询考勤记录 |
| POST | `/attendance/search` | 按条件分页查询考勤记录 |
| GET | `/attendance/statistics/student/{studentId}` | 查询学生出勤统计 |

### 7.4 课程接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/course` | 新增课程 |
| PUT | `/api/course/{courseId}` | 更新课程 |
| GET | `/api/course` | 查询全部课程 |
| GET | `/api/course/{courseId}` | 按 ID 查询课程 |
| GET | `/api/course/teacher/{teacherId}` | 按教师查询课程 |
| GET | `/api/course/class?className=...` | 按班级查询课程 |
| DELETE | `/api/course/{courseId}` | 删除课程 |

### 7.5 请假接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/leave/apply` | 提交请假申请 |
| POST | `/api/leave/approve/{id}` | 审批请假申请 |
| GET | `/api/leave` | 查询全部请假申请 |
| GET | `/api/leave/{id}` | 按 ID 查询请假申请 |
| GET | `/api/leave/student/{studentId}` | 按学生查询请假申请 |
| GET | `/api/leave/course/{courseId}` | 按课程查询请假申请 |
| GET | `/api/leave/status/{status}` | 按状态查询请假申请 |

## 8. Excel 导入格式

### 8.1 学生信息导入

学生导入读取第一张工作表，并从第二行开始解析：

| Excel 列 | 内容 |
| --- | --- |
| 第 1 列 | 学号 |
| 第 2 列 | 姓名 |
| 第 3 列 | 性别 |
| 第 7 列 | 班级 |

### 8.2 考勤记录导入

考勤导入读取第一张工作表，并从第二行开始解析：

| Excel 列 | 内容 |
| --- | --- |
| 第 1 列 | 学生 ID |
| 第 2 列 | 课程 ID |
| 第 3 列 | 打卡时间 |
| 第 4 列 | 考勤状态 |

上传文件支持 `.xlsx` 和 `.xls` 格式，上传目录由本地 `file.upload.path` 配置决定。

## 9. 测试与 Bug 排查

### 9.1 当前测试资源

项目在 `test/` 目录中提供了多个 `.http` 文件，可使用 IntelliJ IDEA HTTP Client 或 VS Code REST Client 执行：

- `test/security-test.http`：注册与登录测试
- `test/test1.http`：用户管理接口测试
- `test/test2.http`：学生与考勤基础接口测试
- `test/attendance-specification-test.http`：考勤条件查询测试
- `test/course-leave-test.http`：课程管理与请假申请接口测试

当前自动化测试包含 Spring Boot 上下文加载测试，以及课程、请假 Service 的部分业务规则测试；后续仍需补充 Controller、Repository 和更多异常场景测试。

运行自动化测试：

```bash
./mvnw test
```

### 9.2 测试检查清单

- [ ] 所有 Controller 接口测试通过
- [ ] Service 层业务逻辑测试通过
- [ ] 数据库操作测试通过
- [ ] 登录失败、重复注册、空参数等异常场景测试通过
- [ ] 时间范围、分页边界、空查询结果等边界条件测试通过
- [ ] Excel 文件为空、格式错误、数据错误等导入场景测试通过
- [ ] 大数据量查询和导入性能测试通过

### 9.3 常见问题排查

| 问题 | 常见原因 | 排查建议 |
| --- | --- | --- |
| `400 Bad Request` | 参数格式错误、类型不匹配、请求体不符合要求 | 检查 `@RequestParam`、`@RequestBody` 和前端参数格式 |
| `404 Not Found` | URL 错误、Controller 映射错误、静态资源路径错误 | 检查请求路径、`@RequestMapping` 和资源目录 |
| `500 Internal Server Error` | 空指针、类型转换失败、数据库异常、业务逻辑异常 | 查看控制台堆栈，定位最早出现的项目代码行 |
| 数据库连接失败 | 数据库服务未启动、地址或账号错误、网络不可达 | 检查本地 `application.properties` 和数据库连接权限 |
| 登录始终失败 | 数据库中的密码不是 BCrypt 密文 | 使用注册接口创建用户，或确保初始化密码为 BCrypt 格式 |
| 文件上传失败 | `file.upload.path` 未配置、目录无权限、文件过大 | 检查上传目录和 multipart 文件大小限制 |

## 10. 开发注意事项

1. **不要提交敏感配置。** 数据库地址、账号、密码等内容只保存在本地或安全的外部配置中。
2. **注意两类学生标识。** `Student` 实体使用字符串学号作为主键，考勤记录中的 `studentId` 当前为数字类型，并与用户数据关联。开发新功能前应确认使用的是学生档案学号还是用户 ID。
3. **注意密码格式。** 注册接口使用 BCrypt 保存密码，手工插入用户数据时也应使用 BCrypt 密文。
4. **不要把“已集成 Spring Security”等同于“已完成权限控制”。** 当前配置允许所有请求访问，真实环境必须继续完善认证状态和角色授权。
5. **导入格式与代码约定必须保持一致。** 修改 Excel 模板列顺序时，需要同步修改导入解析逻辑。
6. **优先统一异常处理。** Controller 中不应重复散落大量异常捕获逻辑，业务异常和系统异常应通过全局异常处理器统一返回。

## 11. 项目交付准备

### 11.1 交付物清单

#### 代码部分

- [x] 源代码
- [x] 数据库脚本
- [x] Maven 依赖文件 `pom.xml`
- [x] 脱敏后的配置示例
- [ ] 可执行 JAR 包

#### 文档部分

- [x] 项目说明文档 `README.md`
- [ ] 数据库设计文档
- [ ] 完整接口文档
- [ ] 部署文档
- [ ] 用户使用手册
- [ ] 测试报告

#### 演示与验收

- [ ] 演示视频
- [ ] 所有已实现功能完成手工验收
- [ ] 自动化测试通过
- [ ] 无致命 Bug
- [x] 敏感配置未提交到 Git 仓库

### 11.2 验收重点

- **功能完整性：** 已实现功能能够正常使用，未实现功能有明确说明。
- **稳定性：** 常见操作、异常输入和边界条件不会导致系统崩溃。
- **安全性：** 密码使用 BCrypt 保存，敏感配置不进入仓库，权限控制符合角色要求。
- **性能：** 列表查询、条件筛选和 Excel 导入在合理数据量下响应正常。
- **可维护性：** 代码分层清晰，命名规范，异常处理和日志便于排查问题。
- **文档完整性：** README、数据库脚本、接口说明和测试记录能够支持项目交付。

## 12. 后续学习与优化建议

1. 先阅读 `entity/` 和 `sql/`，理清用户、学生、课程和考勤数据之间的关系。
2. 阅读 `controller/`，区分返回 JSON 的 `@RestController` 和返回页面的 `@Controller`。
3. 阅读 `service/impl/`，重点理解学生搜索、考勤条件查询、统计和 Excel 导入逻辑。
4. 阅读 `dao/` 与 `repository/`，对比 JdbcTemplate 和 Spring Data JPA 两种数据访问方式。
5. 优先补充自动化测试，再根据测试结果修复 Bug 和重构代码。
6. 继续完善角色权限、统一异常处理、日志记录、数据模型统一和性能优化。

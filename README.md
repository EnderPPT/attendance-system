# 学生考勤管理系统

## 项目简介

学生考勤管理系统是一个基于 Spring Boot 的 Java Web 项目，提供用户认证、学生管理、课程管理、考勤打卡、考勤记录查询、请假申请与审批等功能。系统同时提供 Thymeleaf 管理页面和 REST API，适合用于班级或课程场景下的考勤数据维护。

## 功能模块

- **用户认证**：用户注册、登录与 BCrypt 密码校验。
- **学生管理**：学生信息新增、查询、编辑、删除、批量删除、搜索、排序和 Excel 导入。
- **课程管理**：课程信息新增、查询、编辑、删除，支持按教师和班级查询。
- **考勤管理**：课程考勤打卡、条件筛选、分页查询、出勤统计和 Excel 导入。
- **请假管理**：请假申请、审批以及按学生、课程、状态查询。
- **页面联动**：课程列表可直接进入对应课程的打卡、考勤记录和请假申请页面，各管理页面提供统一导航。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 开发语言 | Java 17 |
| 核心框架 | Spring Boot、Spring MVC、Spring Security |
| 页面模板 | Thymeleaf |
| 数据访问 | Spring Data JPA、JdbcTemplate |
| 数据库 | PostgreSQL |
| Excel 处理 | Apache POI |
| 工具库 | Lombok |
| 项目构建 | Maven |
| 测试框架 | JUnit、Mockito、Spring Boot Test |

## 项目结构

```text
attendance-system/
├── pom.xml
├── sql/                         # 数据库建表与示例数据脚本
├── test/                        # HTTP 接口请求示例
└── src/
    ├── main/
    │   ├── java/com/example/attendance/
    │   │   ├── common/          # 通用返回结果
    │   │   ├── config/          # 安全、异常与静态资源配置
    │   │   ├── controller/      # REST API 与页面控制器
    │   │   ├── dao/             # JdbcTemplate 数据访问
    │   │   ├── dto/             # 请求、查询与统计对象
    │   │   ├── entity/          # JPA 实体
    │   │   ├── repository/      # Spring Data JPA Repository
    │   │   └── service/         # 业务逻辑
    │   └── resources/
    │       ├── static/           # CSS 等静态资源
    │       └── templates/        # Thymeleaf 页面
    └── test/                    # 自动化测试
```

## 环境要求

- JDK 17
- Maven 3.9 或使用项目自带的 Maven Wrapper
- PostgreSQL

## 数据库初始化

1. 先在 PostgreSQL 中创建数据库：`CREATE DATABASE attendance_system ENCODING 'UTF8';`。
2. 连接到 `attendance_system` 数据库后执行 `sql/attendance.sql`，创建用户、课程、考勤和请假相关表。
3. 如需学生示例数据，可执行 `sql/student.sql`。
4. 默认数据源为 `jdbc:postgresql://localhost:5432/attendance_system`，用户名和密码均为 `postgres`。实际部署时请通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量覆盖，上传目录可通过 `UPLOAD_PATH` 覆盖。

初始化脚本中的示例账号密码均为 `123456`。首次登录后应立即修改或删除这些示例账号，生产环境不要保留默认密码。

## 启动项目

Linux 或 macOS：

```bash
./mvnw spring-boot:run
```

Windows：

```bat
mvnw.cmd spring-boot:run
```

项目默认端口为 `8085`，启动后访问：

```text
http://localhost:8085/login
```

## 主要页面

| 页面 | 地址 |
| --- | --- |
| 系统首页 | `/dashboard` |
| 学生管理 | `/student/page/list` |
| 课程管理 | `/course/page/list` |
| 考勤打卡 | `/attendance/page/checkin` |
| 考勤记录 | `/attendance/page/list` |
| 提交请假 | `/leave/page/apply` |
| 请假管理 | `/leave/page/list` |

## REST API

REST API 使用统一的 `Result<T>` 返回格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

### 课程接口

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| `POST` | `/api/course` | 新增课程 |
| `PUT` | `/api/course/{courseId}` | 更新课程 |
| `GET` | `/api/course/{courseId}` | 查询课程详情 |
| `GET` | `/api/course` | 查询全部课程 |
| `GET` | `/api/course/teacher/{teacherId}` | 按教师查询课程 |
| `GET` | `/api/course/class?className=...` | 按班级查询课程 |
| `DELETE` | `/api/course/{courseId}` | 删除课程 |

### 请假接口

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| `POST` | `/api/leave/apply` | 提交请假申请 |
| `POST` | `/api/leave/approve/{id}?approved=true` | 审批请假申请 |
| `GET` | `/api/leave/{id}` | 查询申请详情 |
| `GET` | `/api/leave` | 查询全部申请 |
| `GET` | `/api/leave/student/{studentId}` | 按学生查询申请 |
| `GET` | `/api/leave/course/{courseId}` | 按课程查询申请 |
| `GET` | `/api/leave/status/{status}` | 按状态查询申请 |

更多请求示例见 `test/` 目录中的 `.http` 文件。

## 运行测试

```bash
./mvnw test
```

# 班级考勤管理系统

## 项目简介

班级考勤管理系统是一个基于 Spring Boot 的 Web 应用，用于管理一个班级的日常考勤记录。系统分管理员、教师、学生三种角色，包含学生信息、课程、选课、打卡、请假、考勤统计等模块。

## 技术栈

- **后端**：Spring Boot 4.0、Spring MVC、Spring Security
- **数据访问**：Spring Data JPA、JdbcTemplate
- **前端**：Thymeleaf、原生 CSS
- **数据库**：PostgreSQL
- **Excel 导入**：Apache POI
- **测试**：JUnit 5、Mockito
- **构建工具**：Maven

## 功能特性

- 用户登录与权限管理，密码 BCrypt 加密，拦截器按角色控制访问范围
- 学生信息管理，支持搜索、排序、分页、批量删除和 Excel 批量导入
- 导入或新增学生时自动创建登录账号，首次登录需修改密码
- 课程管理，课程包含教室座位布局、上课时间、迟到阈值等配置
- 选课管理，支持按学号单个或批量选课
- 考勤打卡，通过教室座位图点选座位完成
- 请假申请与教师审批
- 考勤记录查询，支持按课程、状态、日期筛选

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- PostgreSQL 12+

### 安装步骤

1. 克隆项目到本地。

2. 创建数据库，并执行建表脚本：

   ```sql
   CREATE DATABASE attendance_system ENCODING 'UTF8';
   ```

   连上 `attendance_system` 后执行 `sql/attendance.sql`，会建好所有表并写入示例数据。

3. 修改配置文件 `src/main/resources/application.properties`，确认数据库连接：

   ```
   spring.datasource.url=jdbc:postgresql://localhost:5432/attendance_system
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   server.port=8085
   ```

   这几项也可以用环境变量 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`SERVER_PORT` 覆盖。

4. 运行项目：

   ```bash
   ./mvnw spring-boot:run        # Linux / macOS
   mvnw.cmd spring-boot:run      # Windows
   ```

   启动后访问 `http://localhost:8085/login`。

### 示例账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | admin | 123456 |
| 教师 | t_wang | 123456 |
| 学生 | s_001 ~ s_005 | 123456 |

通过导入或新增学生创建的账号，初始密码也是 123456，但第一次登录必须先改密码才能进系统。

## 部署说明

项目打成 jar 后可以直接在服务器运行：

```bash
java -jar attendance-system.jar
```

## 数据库文档

### sys_user（用户表）

| 字段名 | 类型 | 说明 | 备注 |
| --- | --- | --- | --- |
| id | BIGSERIAL | 主键 | 自增 |
| username | VARCHAR(50) | 用户名 | 唯一，学生账号即学号 |
| password | VARCHAR(100) | 密码 | BCrypt 加密 |
| real_name | VARCHAR(50) | 姓名 | - |
| role | VARCHAR(20) | 角色 | ADMIN / TEACHER / STUDENT |
| must_change_password | BOOLEAN | 是否强制改密 | 首次登录用 |
| create_time | TIMESTAMP | 创建时间 | - |

### student（学生表）

| 字段名 | 类型 | 说明 | 备注 |
| --- | --- | --- | --- |
| student_id | VARCHAR(50) | 学号 | 主键 |
| name | VARCHAR(50) | 姓名 | - |
| class_name | VARCHAR(50) | 班级 | - |
| age | INT | 年龄 | - |
| gender | VARCHAR(10) | 性别 | - |
| birth_date | VARCHAR(50) | 出生日期 | - |
| phone | VARCHAR(20) | 电话 | - |

### course（课程表）

| 字段名 | 类型 | 说明 | 备注 |
| --- | --- | --- | --- |
| course_id | BIGSERIAL | 主键 | 自增 |
| course_code | VARCHAR(20) | 课程代码 | 唯一 |
| course_name | VARCHAR(50) | 课程名称 | - |
| class_name | VARCHAR(50) | 班级 | - |
| teacher_id | BIGINT | 教师 ID | 外键，指向 sys_user |
| classroom_name | VARCHAR(50) | 教室 | - |
| layout_rows | INT | 教室行数 | - |
| layout_cols | INT | 教室列数 | - |
| exclude_seats | VARCHAR(200) | 排除座位 | 格式 行,列;行,列 |
| weekday | INT | 上课星期 | - |
| start_time | TIME | 上课开始时间 | - |
| end_time | TIME | 上课结束时间 | - |
| late_threshold_minutes | INT | 迟到阈值 | 单位分钟，默认 15 |
| start_week / end_week | INT | 起止教学周 | - |

### course_selection（选课表）

| 字段名 | 类型 | 说明 | 备注 |
| --- | --- | --- | --- |
| id | BIGSERIAL | 主键 | 自增 |
| course_id | BIGINT | 课程 ID | 外键 |
| student_id | BIGINT | 学生用户 ID | 外键，指向 sys_user |
| select_time | TIMESTAMP | 选课时间 | - |

`(course_id, student_id)` 唯一，防止重复选课。

### attendance（考勤记录表）

| 字段名 | 类型 | 说明 | 备注 |
| --- | --- | --- | --- |
| id | BIGSERIAL | 主键 | 自增 |
| course_id | BIGINT | 课程 ID | 外键 |
| student_id | BIGINT | 学生用户 ID | 外键 |
| check_in_time | TIMESTAMP | 打卡时间 | - |
| seat_row | INT | 座位行 | - |
| seat_col | INT | 座位列 | - |
| status | VARCHAR(20) | 状态 | NORMAL / LATE / EARLY / ABSENT / LEAVE |
| ip | VARCHAR(45) | 打卡 IP | - |
| create_time | TIMESTAMP | 创建时间 | - |

两个唯一索引：
- course_id + student_id + 当天：同一学生同一课程一天只能打一次卡
- course_id + seat_row + seat_col + 当天：同一课程同一座位一天只能被一个人用

### leave_application（请假申请表）

| 字段名 | 类型 | 说明 | 备注 |
| --- | --- | --- | --- |
| id | BIGSERIAL | 主键 | 自增 |
| student_id | BIGINT | 学生用户 ID | 外键 |
| course_id | BIGINT | 课程 ID | 外键 |
| start_time / end_time | TIMESTAMP | 请假起止时间 | - |
| reason | VARCHAR(500) | 请假事由 | - |
| status | VARCHAR(20) | 审批状态 | PENDING / APPROVED / REJECTED |
| apply_time | TIMESTAMP | 申请时间 | - |
| approval_time | TIMESTAMP | 审批时间 | - |
| approver_remark | VARCHAR(500) | 审批备注 | - |

## 已知限制

- 为方便教学演示关掉了 CSRF 防护
- 只写了 Service 层的单元测试，没有 Controller 层的集成测试

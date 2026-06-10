# 班级考勤管理系统

基于 Spring Boot 的班级考勤管理系统，覆盖学生、课程、选课、打卡、请假、统计、Excel 导入等场景，区分管理员、教师、学生三种角色。考勤打卡通过可视化教室座位图完成，请假审批通过后自动联动考勤记录，迟到/缺勤状态根据每门课程配置的上课时间动态判定。

## 开发者信息

- **姓名：** 叶黎明
- **学号：** 42411171

## 功能概览

### 账号与权限
- 用户名密码登录，密码使用 BCrypt 加密保存
- 自定义拦截器基于 Session 校验登录态，学生路径白名单限制访问范围
- 导入或新增学生时自动建立登录账号（用户名即学号，初始密码 `123456`）
- 新建账号首次登录强制修改密码，未修改前无法访问其他功能
- 任意角色登录后均可在导航中主动修改自己的密码

### 学生与课程
- 学生档案的增删改查、关键字搜索、按字段排序、分页与批量删除
- 课程包含课程代码、教师、教室、座位行列、排除座位、上课时间、迟到阈值、教学周等字段
- 教师必须是 `TEACHER` 或 `ADMIN` 角色，课程代码全局唯一
- 通过 Apache POI 解析 Excel 批量导入学生，失败行收集错误信息单独反馈

### 选课
- 学生与课程多对多关联，管理员或教师为学生分配课程
- 学生登录后可查看自己已选课程，并跳转到打卡或请假页
- 打卡和请假都会校验学生是否选了该课程，从业务上闭环

### 考勤打卡
- 选定课程后，前端调用 `/seat/layout/{courseId}` 拉取教室行列数、排除座位和当天已占用座位，渲染为可视化座位图
- 座位状态用颜色区分：白色可选、绿色已选中、红色已被占用、灰色不可用，杜绝输入错误
- 学生身份从 Session 取出，无法代他人打卡
- Service 层校验座位范围、是否在排除列表、是否选课、当天是否重复打卡、座位当天是否被占用
- 数据库 `(course_id, student_id, day)` 与 `(course_id, seat_row, seat_col, day)` 唯一索引兜底
- 打卡状态根据课程配置的上课时间与迟到阈值动态判定（NORMAL / LATE / ABSENT）

### 请假
- 学生提交请假申请，校验：未来时间、不超过 3 天、原因非空、所选课程在自己的选课名单内
- 管理员或教师审批通过或驳回，可填写审批备注
- 审批通过时，系统遍历请假覆盖的日期，对每一天没有打卡的对应课程自动写入一条 `LEAVE` 状态的考勤记录，避免被统计为缺勤
- 学生可在「我的请假」查看自己提交过的申请及审批状态

### 考勤记录与统计
- 支持按课程、状态、日期范围筛选，分页展示
- 状态包含 NORMAL / LATE / EARLY / ABSENT / LEAVE，列表用不同颜色徽章区分
- 提供按学生维度的出勤次数与出勤率统计接口

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 开发语言 | Java 17 |
| 核心框架 | Spring Boot 4.x、Spring MVC、Spring Security |
| 数据访问 | Spring Data JPA、JdbcTemplate |
| 页面模板 | Thymeleaf |
| 数据库 | PostgreSQL |
| Excel 处理 | Apache POI 5.x |
| 工具库 | Lombok |
| 构建工具 | Maven（含 Maven Wrapper） |
| 测试框架 | JUnit 5、Mockito、Spring Boot Test |

## 项目结构

```text
attendance-system/
├── pom.xml
├── sql/                         # 数据库建表与示例数据脚本
├── test/                        # HTTP 接口请求示例
└── src/
    ├── main/
    │   ├── java/com/example/attendance/
    │   │   ├── common/          # 统一返回结果
    │   │   ├── config/          # 安全、拦截器、静态资源配置
    │   │   ├── controller/      # 页面控制器与 REST 控制器
    │   │   ├── dao/             # JdbcTemplate 数据访问
    │   │   ├── dto/             # 请求、查询与统计对象
    │   │   ├── entity/          # JPA 实体
    │   │   ├── exception/       # 业务异常与统一处理
    │   │   ├── repository/      # Spring Data JPA Repository
    │   │   └── service/         # 业务逻辑与实现
    │   └── resources/
    │       ├── application.properties
    │       ├── static/css/      # 样式表
    │       └── templates/       # Thymeleaf 页面
    └── test/                    # 服务层单元测试
```

## 数据库

主要表：

| 表 | 用途 |
| --- | --- |
| `sys_user` | 登录账号，区分 ADMIN / TEACHER / STUDENT，含首次登录改密标记 |
| `student` | 学生档案信息 |
| `course` | 课程，含教室座位布局、上课时间、迟到阈值 |
| `course_selection` | 学生与课程的多对多关联 |
| `attendance` | 考勤记录，含座位、状态等字段 |
| `leave_application` | 请假申请，含审批状态与备注 |

`attendance` 表上的两个唯一索引：

- `uk_attendance_course_student_day`：同一学生同一课程同一天只能有一条记录
- `uk_attendance_course_seat_day`：同一课程同一座位同一天只能被一个学生使用

## 环境要求

- JDK 17
- Maven 3.9+（也可直接使用项目自带的 `mvnw` / `mvnw.cmd`）
- PostgreSQL 12+

## 数据库初始化

1. 在 PostgreSQL 中创建数据库：

   ```sql
   CREATE DATABASE attendance_system ENCODING 'UTF8';
   ```

2. 切换到 `attendance_system` 数据库，执行 `sql/attendance.sql`，创建所有业务表并写入示例数据。

3. 默认连接配置：

   ```
   spring.datasource.url=jdbc:postgresql://localhost:5432/attendance_system
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

   可通过环境变量 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 覆盖，文件上传目录用 `UPLOAD_PATH` 覆盖，服务端口用 `SERVER_PORT` 覆盖。

> 示例账号密码全部为 `123456`，仅用于演示。

## 启动项目

Linux / macOS：

```bash
./mvnw spring-boot:run
```

Windows：

```bat
mvnw.cmd spring-boot:run
```

启动后访问：

```
http://localhost:8085/login
```

## 示例账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 教师 | `t_wang` | `123456` |
| 学生 | `s_001` ~ `s_005` | `123456` |

通过学生导入或新增学生新建的账号，初始密码也是 `123456`，但**首次登录会被强制要求修改密码**才能进入系统。

## 主要页面

| 页面 | 地址 | 可见角色 |
| --- | --- | --- |
| 系统首页 | `/dashboard` | 全部 |
| 修改密码 | `/page/password` | 全部 |
| 学生管理 | `/student/page/list` | 管理员 / 教师 |
| 学生导入 | `/student/page/import` | 管理员 / 教师 |
| 课程管理 | `/course/page/list` | 管理员 / 教师 |
| 选课管理 | `/selection/page/list` | 管理员 / 教师 |
| 我的课程 | `/selection/page/my` | 学生 |
| 考勤打卡 | `/attendance/page/checkin` | 学生（管理员可代录） |
| 考勤记录 | `/attendance/page/list` | 管理员 / 教师 |
| 考勤导入 | `/attendance/page/import` | 管理员 / 教师 |
| 提交请假 | `/leave/page/apply` | 学生 |
| 我的请假 | `/leave/page/my` | 学生 |
| 请假审批 | `/leave/page/list` | 管理员 / 教师 |

## REST API

接口统一返回 `Result<T>` 结构：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

### 座位布局

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| `GET` | `/seat/layout/{courseId}` | 返回课程行列数、排除座位、当天已占用座位 |

### 课程

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| `POST` | `/api/course` | 新增课程 |
| `PUT` | `/api/course/{courseId}` | 更新课程 |
| `GET` | `/api/course/{courseId}` | 查询课程详情 |
| `GET` | `/api/course` | 查询全部课程 |
| `GET` | `/api/course/teacher/{teacherId}` | 按教师查询课程 |
| `GET` | `/api/course/class?className=...` | 按班级查询课程 |
| `DELETE` | `/api/course/{courseId}` | 删除课程 |

### 请假

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

服务层覆盖了座位边界、重复打卡、状态规范化、课程代码唯一、请假时长、重复审批等核心逻辑。`AttendanceSystemApplicationTests` 需要可用的 PostgreSQL 连接，若仅运行服务层单元测试可通过 `-Dtest=...` 指定。

## 已知限制

- 出勤率分母为已有考勤记录数，未按课程名单计算应出勤次数
- CSRF 防护已关闭，仅作教学示例使用
- 缺少 Controller 层的 MockMvc 集成测试

# 学生考勤管理系统

## 开发者信息
- **姓名：** 叶黎明
- **学号：** 42411171
- **课程：** JAVAEE开发实践

## 📖 项目简介
本项目是基于 Spring Boot 框架开发的轻量级学生考勤管理系统。项目作为课程实践任务，主要用于练习和巩固企业级 Java Web 开发的核心规范与基础流程。

## ✨ 核心特性与技术要点
本项目严格遵循了企业标准开发规范，主要实现了以下技术要点：

1. **标准三层架构：**
    - 实现了清晰的代码分层：`Controller` (控制层) -> `Service` (业务逻辑层) -> `Dao` (数据访问层)。
    - 使用 `@RestController`、`@Service`、`@Repository` 及 `@Autowired` 实现了面向接口的依赖注入。
2. **统一响应格式：**
    - 封装了全局统一的 `Result<T>` 响应工具类，规范了前后端数据交互格式（包含状态码、提示信息和数据体）。
3. **代码精简：**
    - 引入 Lombok 插件，通过 `@Data` 等注解自动生成 Getter/Setter 及构造函数，保持实体类代码整洁。

## 📁 核心工程结构
```text
src/main/java/com/example/attendance/
 ├── common/          # 公共组件（如统一响应实体 Result）
 ├── controller/      # 控制器层（接收请求、返回响应）
 ├── service/         # 业务逻辑层接口及实现类 (impl)
 ├── dao/             # 数据访问层（数据库操作）
 └── entity/          # 数据实体类（如 Student 配置 Lombok）
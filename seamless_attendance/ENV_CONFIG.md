# 多环境配置说明

## 环境配置文件

项目现在支持三个环境：

1. **开发环境 (dev)** - `application-dev.properties`
   - 端口：8081
   - 数据库：seamless_attendance_dev
   - 日志级别：DEBUG
   - JPA DDL：update（自动更新表结构）

2. **测试环境 (test)** - `application-test.properties`
   - 端口：8082
   - 数据库：seamless_attendance_test
   - 日志级别：INFO
   - JPA DDL：none（不自动更新表结构）

3. **生产环境 (prod)** - `application-prod.properties`
   - 端口：8080
   - 数据库：seamless_attendance
   - 日志级别：WARN
   - JPA DDL：none（不自动更新表结构）
   - 日志文件：logs/seamless_attendance.log

## 如何切换环境

### 方法 1：修改 application.properties

编辑 `application.properties` 文件，修改 `spring.profiles.active` 的值：

```properties
# 开发环境
spring.profiles.active=dev

# 测试环境
spring.profiles.active=test

# 生产环境
spring.profiles.active=prod
```

### 方法 2：启动时指定环境

在启动应用时通过命令行参数指定环境：

```bash
# 开发环境
java -jar seamless_attendance.jar --spring.profiles.active=dev

# 测试环境
java -jar seamless_attendance.jar --spring.profiles.active=test

# 生产环境
java -jar seamless_attendance.jar --spring.profiles.active=prod
```

### 方法 3：在 IDE 中指定环境

在 IDE 的运行配置中添加 VM 参数：

```
-Dspring.profiles.active=dev
```

或者在 Program Arguments 中添加：

```
--spring.profiles.active=dev
```

## 环境配置说明

### 开发环境 (dev)
- 适合本地开发和调试
- 日志级别设置为 DEBUG，可以看到详细的日志信息
- JPA DDL 设置为 update，自动更新表结构
- 数据库连接使用开发环境的数据库

### 测试环境 (test)
- 适合测试团队使用
- 日志级别设置为 INFO，只显示重要信息
- JPA DDL 设置为 none，不自动更新表结构
- 数据库连接使用测试环境的数据库

### 生产环境 (prod)
- 适合正式上线使用
- 日志级别设置为 WARN，只显示警告和错误
- JPA DDL 设置为 none，不自动更新表结构
- 数据库连接使用生产环境的数据库
- 启用日志文件，日志会写入文件并自动滚动

## 数据库准备

在切换环境之前，需要先创建对应的数据库：

```sql
-- 开发环境数据库
CREATE DATABASE seamless_attendance_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 测试环境数据库
CREATE DATABASE seamless_attendance_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 生产环境数据库
CREATE DATABASE seamless_attendance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 安全建议

1. **不要将敏感信息提交到版本控制系统**
   - 数据库密码
   - MQTT 密码
   - API 密钥等

2. **生产环境配置**
   - 使用强密码
   - 启用 SSL 连接
   - 配置日志文件滚动策略

3. **环境变量配置**
   - 可以考虑使用环境变量来存储敏感信息
   - 例如：`spring.datasource.password=${DB_PASSWORD}`

## 验证当前环境

启动应用后，可以在日志中看到当前激活的环境：

```
The following 1 profile is active: "dev"
```

或者通过代码获取当前环境：

```java
@Value("${spring.profiles.active}")
private String activeProfile;
```

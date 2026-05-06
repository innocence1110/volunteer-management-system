# 🧑‍🤝‍🧑 志愿活动管理系统

> 基于 Spring Boot + Vue 3 的数字化志愿活动管理平台，涵盖活动发布、报名、多种方式签到、积分激励、实时通知等完整业务流程。

---

## 📋 目录

- [项目简介](#项目简介)
- [功能概览](#功能概览)
- [技术栈](#技术栈)
- [环境安装](#环境安装)
- [项目克隆与启动](#项目克隆与启动)
- [默认账号](#默认账号)
- [操作指南](#操作指南)
- [项目结构](#项目结构)
- [API 接口](#api-接口)
- [配置说明](#配置说明)
- [常见问题](#常见问题)
- [部署说明](#部署说明)

---

## 项目简介

本系统是一个 B/S 架构的志愿活动管理平台，支持**管理员**和**志愿者**两种角色。

- **管理员**：发布/管理活动，查看报名和签到数据，查看GPS地址和签到照片
- **志愿者**：浏览/搜索活动，在线报名，三种方式签到，查看积分和通知

---

## 功能概览

### 管理员端

| 功能 | 说明 |
|------|------|
| 系统概览 | 用户数、活动数、报名数、签到数统计 |
| 活动发布 | 设置名称、时间、地点、人数、签到方式、奖励积分 |
| 活动管理 | 修改/删除活动，查看报名列表和签到详情 |
| 活动浏览 | 分页搜索所有活动 |
| 信息管理 | 查看/修改个人信息 |
| 通知消息 | 查看系统通知，已读/未读管理 |

### 志愿者端

| 功能 | 说明 |
|------|------|
| 我的主页 | 积分、已报名数、已签到数统计 |
| 活动浏览 | 搜索活动、查看详情 |
| 活动报名 | 在线报名、取消报名（活动开始前2小时） |
| 活动签到 | 按钮签到（GPS）、数字码签到、图片签到 |
| 信息管理 | 查看/修改个人信息 |
| 通知消息 | 查看系统通知 |

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Java + Spring Boot | 2.7.18 |
| 持久层 | MyBatis-Plus | 3.5.3.1 |
| 数据库 | MySQL | 8.0+ |
| 前端框架 | Vue 3 | 3.3.x |
| UI 组件库 | Element Plus | 2.4.x |
| 状态管理 | Pinia | 2.1.x |
| HTTP 客户端 | Axios | 1.6.x |
| 接口文档 | SpringDoc (Swagger UI) | 1.7.0 |
| 认证 | JWT (jjwt) | 0.11.5 |
| 密码加密 | BCrypt | 5.7.x |

---

## 环境安装

在克隆项目之前，请先安装以下软件。**任意一项未安装都会导致项目无法运行。**

### 1. JDK 11+

项目使用 Java 17，需要 JDK 11 或更高版本。

**macOS：**
```bash
brew install openjdk@17
```

**Windows：**
从 [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 或 [Adoptium](https://adoptium.net/) 下载安装。

**验证：**
```bash
java -version
# 输出 java version "17.x.x" 即可
```

### 2. Maven 3.6+

用于构建后端项目和管理依赖。

**macOS：**
```bash
brew install maven
```

**Windows：**
从 [Apache Maven](https://maven.apache.org/download.cgi) 下载，解压后将 `bin` 目录加入系统 PATH。

**验证：**
```bash
mvn -version
# 输出 Apache Maven 3.9.x 即可
```

### 3. Node.js 16+

用于运行前端开发服务器。

**macOS / Windows：**
访问 [Node.js 官网](https://nodejs.org/)，下载 LTS 版本安装即可（自带 npm）。

**验证：**
```bash
node -v    # 输出 v18.x.x 或更高
npm -v     # 输出 9.x.x 或更高
```

### 4. MySQL 8.0+

用于存储项目数据。

**macOS：**
```bash
brew install mysql
brew services start mysql
# 设置 root 密码（首次安装后执行）
mysql_secure_installation
```

**Windows：**
从 [MySQL Community Server](https://dev.mysql.com/downloads/mysql/) 下载安装，安装时设置 root 密码。

**验证：**
```bash
mysql -u root -p
# 输入密码后能进入 mysql 命令行即可，输入 exit 退出
```

---

## 项目克隆与启动

### 第1步：克隆项目

```bash
git clone https://github.com/你的用户名/volunteer-management-system.git
cd volunteer-management-system
```

### 第2步：初始化数据库

```bash
# 将项目中的 SQL 脚本导入 MySQL，创建数据库和表
# 请将 root 和 123456 替换为你自己的 MySQL 用户名和密码
mysql -u root -p123456 < database/schema.sql
```

执行成功后会自动创建 `volunteer_system` 数据库和 5 张数据表，以及 2 个测试账号。

### 第3步：修改后端数据库配置

编辑 `backend/src/main/resources/application.yml`，将数据库用户名和密码改为你自己的：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/volunteer_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root          # ← 改成你的 MySQL 用户名
    password: 123456        # ← 改成你的 MySQL 密码
```

### 第4步：启动后端

```bash
cd backend

# 如果 mvn 命令不可用，先设置 PATH（macOS Homebrew 安装的情况）
export PATH="/usr/local/apache-maven-3.9.15/bin:$PATH"

# 首次启动会下载依赖包（约 200MB），需要几分钟
mvn spring-boot:run
```

看到以下信息表示启动成功：
```
Started VolunteerApplication in 1.xxx seconds
```

后端运行在 http://localhost:8080

### 第5步：启动前端

**另开一个终端**（不要关闭后端终端）：

```bash
cd frontend

# 首次需要安装依赖（约 1-2 分钟）
npm install

# 启动开发服务器
npm run dev
```

看到以下信息表示启动成功：
```
➜  Local:   http://localhost:3000/
```

### 第6步：访问系统

打开浏览器访问 **http://localhost:3000**，用默认账号登录即可。

---

## 默认账号

| 角色 | 账号 | 密码 | 说明 |
|------|------|------|------|
| 管理员 | admin | admin123 | 系统预置管理员 |
| 志愿者 | volunteer | 123456 | 系统预置志愿者 |

也可以在登录页面点击"注册账号"创建新用户。

---

## 操作指南

### 项目关闭

```bash
# 关闭后端：在后端终端按 Ctrl+C，或执行
lsof -ti :8080 | xargs kill -9

# 关闭前端：在前端终端按 Ctrl+C，或执行
lsof -ti :3000 | xargs kill -9
```

### 重新启动

```bash
# 重新启动后端
cd backend
mvn spring-boot:run

# 重新启动前端（另开终端）
cd frontend
npm run dev
```

### 数据库查询

```bash
# 登录数据库
mysql -u root -p123456

# 进入项目数据库
USE volunteer_system;

# 查看所有用户
SELECT id, name, account, role, points FROM users;

# 查看所有活动
SELECT id, name, location, start_time, end_time, status FROM activities;

# 查看报名记录（含志愿者姓名和活动名称）
SELECT u.name AS 志愿者, a.name AS 活动, r.status, r.create_time
FROM registrations r
JOIN users u ON r.user_id = u.id
JOIN activities a ON r.activity_id = a.id;

# 查看签到记录
SELECT u.name AS 志愿者, a.name AS 活动, c.check_in_type, c.check_in_time, c.gps_address
FROM check_ins c
JOIN users u ON c.user_id = u.id
JOIN activities a ON c.activity_id = a.id;

# 查看积分排行榜
SELECT name, points FROM users WHERE role = 'volunteer' ORDER BY points DESC;
```

### 数据库清空与重置

```bash
# 完全重置数据库（删除所有数据并重建）
mysql -u root -p123456 -e "DROP DATABASE IF EXISTS volunteer_system;"
mysql -u root -p123456 < database/schema.sql
```

```bash
# 只清空数据表（保留表结构）
mysql -u root -p123456 volunteer_system -e "
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE check_ins;
TRUNCATE TABLE notifications;
TRUNCATE TABLE registrations;
TRUNCATE TABLE activities;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;
"
# 清空后需要重新导入初始数据
mysql -u root -p123456 < database/schema.sql
```

### 数据库备份与恢复

```bash
# 备份
mysqldump -u root -p123456 volunteer_system > backup.sql

# 恢复
mysql -u root -p123456 volunteer_system < backup.sql
```

### 查看后端运行日志

后端控制台会输出所有 SQL 语句和错误信息。如需更详细的日志，编辑 `backend/src/main/resources/application.yml`：

```yaml
logging:
  level:
    com.volunteer: debug
```

### 局域网访问（其他设备）

确保所有设备连接同一 WiFi，其他设备浏览器打开：

```
http://你的电脑IP:3000
```

查看本机 IP：`ipconfig getifaddr en0`（macOS）或 `ipconfig`（Windows）

---

## 项目结构

```
volunteer-management-system/
├── README.md                              项目说明文档
├── .gitignore                             Git 忽略规则
│
├── database/
│   └── schema.sql                         数据库初始化脚本（5张表+初始数据）
│
├── backend/                               Spring Boot 后端
│   ├── pom.xml                            Maven 依赖配置
│   └── src/main/
│       ├── java/com/volunteer/
│       │   ├── VolunteerApplication.java      启动类
│       │   │
│       │   ├── config/                        配置类
│       │   │   ├── CorsConfig.java                跨域配置
│       │   │   ├── MyBatisPlusConfig.java         分页插件 + 自动填充
│       │   │   ├── WebMvcConfig.java              静态资源映射
│       │   │   └── WebMvcInterceptorConfig.java   JWT 拦截器注册
│       │   │
│       │   ├── entity/                      实体类
│       │   │   ├── User.java                     用户（id/姓名/电话/账号/密码/角色/积分...）
│       │   │   ├── Activity.java                 活动（名称/时间/地点/人数/签到方式...）
│       │   │   ├── Registration.java             报名记录
│       │   │   ├── CheckIn.java                  签到记录（GPS地址/图片路径）
│       │   │   └── Notification.java             通知消息
│       │   │
│       │   ├── mapper/                      MyBatis-Plus Mapper 接口
│       │   │   ├── UserMapper.java
│       │   │   ├── ActivityMapper.java
│       │   │   ├── RegistrationMapper.java
│       │   │   ├── CheckInMapper.java
│       │   │   └── NotificationMapper.java
│       │   │
│       │   ├── service/                     服务接口
│       │   │   ├── UserService.java
│       │   │   ├── ActivityService.java
│       │   │   ├── RegistrationService.java
│       │   │   ├── CheckInService.java
│       │   │   ├── NotificationService.java
│       │   │   └── impl/                     服务实现（核心业务逻辑）
│       │   │       ├── UserServiceImpl.java         登录/注册/信息管理
│       │   │       ├── ActivityServiceImpl.java     活动CRUD + 状态自动更新
│       │   │       ├── RegistrationServiceImpl.java 报名/取消 + 通知发送
│       │   │       ├── CheckInServiceImpl.java      三种签到 + 积分发放
│       │   │       └── NotificationServiceImpl.java 通知管理
│       │   │
│       │   ├── controller/                  REST API 控制器
│       │   │   ├── AuthController.java            登录/注册
│       │   │   ├── UserController.java            个人信息
│       │   │   ├── ActivityController.java        活动管理
│       │   │   ├── RegistrationController.java    报名管理
│       │   │   ├── CheckInController.java         签到管理（含图片上传）
│       │   │   ├── NotificationController.java    通知管理
│       │   │   └── StatsController.java           数据统计
│       │   │
│       │   ├── dto/                         数据传输对象
│       │   │   ├── Result.java                    统一响应格式
│       │   │   ├── LoginRequest.java              登录请求
│       │   │   ├── RegisterRequest.java           注册请求
│       │   │   ├── ActivityRequest.java           活动创建/修改请求
│       │   │   ├── CheckInRequest.java            签到请求
│       │   │   └── UserUpdateRequest.java         用户信息更新请求
│       │   │
│       │   └── utils/                       工具类
│       │       ├── JwtUtil.java                   JWT 生成/解析/校验
│       │       └── JwtInterceptor.java            JWT 拦截器
│       │
│       └── resources/
│           └── application.yml              应用配置（端口/数据库/JWT/文件上传）
│
└── frontend/                              Vue 3 前端
    ├── package.json                       npm 依赖声明
    ├── package-lock.json                  依赖版本锁定
    ├── vite.config.js                     Vite 配置（端口/代理/插件）
    ├── index.html                         HTML 入口文件
    └── src/
        ├── main.js                        Vue 入口（注册 Element Plus / 图标 / 路由）
        ├── App.vue                        根组件
        │
        ├── router/
        │   └── index.js                   路由配置 + 登录守卫 + 角色权限
        │
        ├── store/
        │   └── index.js                   Pinia 状态管理（用户/Token/未读数）
        │
        ├── api/                           API 请求层
        │   ├── request.js                 Axios 封装（拦截器/自动Token/错误处理）
        │   └── modules/                   各模块 API
        │       ├── auth.js                    登录/注册
        │       ├── user.js                    个人信息
        │       ├── activity.js                活动 CRUD
        │       ├── registration.js            报名/取消
        │       ├── checkin.js                 签到（按钮/码/图片）
        │       ├── notification.js            通知
        │       └── stats.js                   数据统计
        │
        ├── components/
        │   └── Layout.vue                 侧边栏布局（菜单/顶部导航/通知角标）
        │
        ├── views/
        │   ├── auth/                      认证页面
        │   │   ├── Login.vue                  登录页
        │   │   └── Register.vue               注册页
        │   │
        │   ├── admin/                     管理员页面
        │   │   ├── Dashboard.vue              系统概览（统计数据）
        │   │   ├── ActivityPublish.vue        活动发布
        │   │   ├── ActivityManage.vue         活动管理（修改/删除/报名列表/签到详情）
        │   │   ├── ActivityBrowse.vue         活动浏览
        │   │   ├── Profile.vue                信息管理
        │   │   └── Notifications.vue          通知消息
        │   │
        │   └── volunteer/                 志愿者页面
        │       ├── Dashboard.vue              我的主页（积分/报名/签到统计）
        │       ├── ActivityBrowse.vue         活动浏览（搜索/详情）
        │       ├── ActivityRegister.vue       活动报名（报名/取消）
        │       ├── CheckIn.vue                活动签到（按钮/数字码/图片）
        │       ├── Profile.vue                信息管理
        │       └── Notifications.vue          通知消息
        │
        └── styles/
            └── global.css                 全局样式
```

---

## API 接口

后端启动后访问 Swagger UI 查看所有接口：http://localhost:8080/swagger-ui.html

### 认证接口（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录，返回 Token 和用户信息 |
| POST | /api/auth/register | 用户注册 |

### 用户接口（需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/user/profile | 获取个人信息 |
| PUT | /api/user/profile | 修改个人信息（电话/专业/年龄/学号） |

### 活动接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/activities | 管理员 | 发布活动 |
| PUT | /api/activities/{id} | 管理员 | 修改活动 |
| DELETE | /api/activities/{id} | 管理员 | 删除活动 |
| GET | /api/activities | 登录 | 活动列表（分页+搜索） |
| GET | /api/activities/{id} | 登录 | 活动详情 |
| GET | /api/activities/my | 管理员 | 我发布的活动 |

### 报名接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/registrations/{activityId} | 报名活动 |
| DELETE | /api/registrations/{activityId} | 取消报名 |
| GET | /api/registrations/check/{activityId} | 检查是否已报名 |
| GET | /api/registrations/my | 我的报名列表 |
| GET | /api/registrations/activity/{activityId} | 活动报名列表（管理员） |

### 签到接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/checkin/button/{activityId} | 按钮签到（传 GPS 地址） |
| POST | /api/checkin/code/{activityId} | 数字码签到（传验证码） |
| POST | /api/checkin/image/{activityId} | 图片签到（上传照片） |
| GET | /api/checkin/check/{activityId} | 检查是否已签到 |
| GET | /api/checkin/activity/{activityId} | 活动签到列表（管理员） |

### 通知接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/notifications | 通知列表（分页） |
| PUT | /api/notifications/{id}/read | 标记已读 |
| PUT | /api/notifications/read-all | 全部已读 |
| GET | /api/notifications/unread-count | 未读数量 |

### 统计接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/stats | 管理员 | 用户数/活动数/报名数/签到数 |

---

## 配置说明

### 数据库配置

`backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/volunteer_system?...
    username: root          # MySQL 用户名
    password: 123456        # MySQL 密码
```

### JWT 配置

```yaml
jwt:
  secret: VolunteerManagementSystemSecretKey2024VeryLongSecretForHS256
  expiration: 86400000     # Token 有效期 24 小时（毫秒）
```

### 文件上传配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB        # 单文件最大 5MB
      max-request-size: 10MB    # 请求最大 10MB
```

### 前端代理配置

`frontend/vite.config.js`

```javascript
server: {
  host: '0.0.0.0',       // 绑定所有网卡（支持局域网访问）
  port: 3000,             // 前端端口
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端地址
      changeOrigin: true,
    },
    '/uploads': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

---

## 常见问题

### Q1：Maven 下载依赖很慢

配置阿里云镜像，编辑 `~/.m2/settings.xml`（没有则创建）：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

### Q2：启动后端报错 "Access denied for user"

数据库用户名或密码错误，检查 `application.yml`。

### Q3：启动后端报错 "Unknown database 'volunteer_system'"

数据库未初始化，执行 `mysql -u root -p123456 < database/schema.sql`

### Q4：默认账号登录失败

数据库中的密码哈希可能已损坏，重新初始化：
```bash
mysql -u root -p123456 -e "DROP DATABASE IF EXISTS volunteer_system;"
mysql -u root -p123456 < database/schema.sql
```

### Q5：前端页面空白

1. 确保后端已启动（端口 8080）
2. 打开浏览器控制台（F12）查看错误信息
3. 清除浏览器缓存后刷新

### Q6：图片签到上传失败

确认后端目录下 `uploads/checkin/` 文件夹可写。首次上传会自动创建。

### Q7：GPS 定位获取失败

浏览器在 HTTP 环境下可能限制 GPS。使用 `http://localhost:3000` 访问（localhost 被浏览器信任），或在签到页面手动输入地址。

### Q8：端口被占用

```bash
# 查看并关闭占用端口的进程
lsof -ti :8080 | xargs kill -9    # 关闭后端
lsof -ti :3000 | xargs kill -9    # 关闭前端
```

### Q9：如何重新编译后端

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

### Q10：如何重新安装前端依赖

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

---

## 部署说明

### 后端打包

```bash
cd backend
mvn clean package -DskipTests
# 生成 target/volunteer-management-1.0.0.jar
java -jar target/volunteer-management-1.0.0.jar
```

### 前端打包

```bash
cd frontend
npm run build
# 生成 dist/ 目录，部署到 Nginx 即可
```

### Nginx 参考配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /uploads/ {
        proxy_pass http://localhost:8080;
    }
}
```

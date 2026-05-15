# Gym User Service Platform

Gym User Service Platform 是一个基于 **Spring Boot + Vue 3 + MySQL** 的健身房用户服务平台。项目包含 **用户端、教练端、管理员端** 三套界面，围绕课程预约、私教预约、场地预约、VIP 会员、积分商城、每日打卡、社区互动、排行榜和后台管理等功能进行设计。

本项目默认在本地运行，数据库连接地址为 `localhost:3306/gym_db`，不会连接云端数据库。

---

## 1. 技术栈

| 模块 | 技术 |
|---|---|
| 后端 | Spring Boot 2.7.15、Spring Security、JWT、Spring Data JPA |
| 数据库 | MySQL，本地数据库名 `gym_db` |
| 前端 | Vue 3、Vue Router、Vue I18n、Element Plus、Axios、Vite |
| 构建与运行 | Maven / Maven Wrapper、Node.js、npm |

---

## 2. 最终版本功能概览

### 2.1 用户端

用户端用于普通会员日常使用，主要功能包括：

- 用户注册、登录、找回密码、重置密码
- 首页信息展示：积分、连续打卡、快捷入口、课程推荐、VIP 入口
- 课程预约中心：团课预约、私教预约、预约取消
- 个人中心：个人资料、已预约课程、课程取消、投诉建议、积分记录
- VIP 购买页面：月度、季度、年度 VIP 套餐选择与模拟支付
- 积分商城：商品分类、积分兑换、库存变化、兑换记录
- 每日打卡：今日打卡、连续打卡天数、月度日历、积分奖励、勋章展示
- 社区互动：发布动态、点赞、评论、举报、加入/退出组队
- 排行榜：总排行、本周排行、本月排行
- 中英文切换

### 2.2 教练端

教练端用于教练管理课程、场地和学员信息，主要功能包括：

- 教练登录
- 教练首页：今日课程、待处理提醒、课程统计
- 课程管理：查看课程、上架课程、编辑课程、删除课程
- 场地预约：查看可预约时段、预约场地、查看本人已预约场地、删除未使用的场地预约
- 上架课程时，场地下拉框只显示当前教练已经预约且未被课程占用的场地时段
- 学员信息查看
- 教练信箱：查看用户投诉、课程评价，处理投诉与回复评价
- 教练个人信息、资质、专业领域、通知设置
- 中英文切换

### 2.3 管理员端

管理员端用于系统后台管理，主要功能包括：

- 管理员登录
- 用户管理：新增、编辑、删除用户
- 会员管理：新增会员、编辑会员、删除会员、会员状态统计
- 课程管理：查看、编辑、删除课程，与用户端预约和教练端课程数据联动
- 商品管理：直接管理用户端积分商城商品
  - 新增、编辑、删除商品
  - 设置中文名称、英文名称、中文描述、英文描述
  - 设置商品分类、商品图标、积分价格、库存、上下架状态
  - 中英文切换时，优先显示对应语言的商品名称和描述
- 社区管理：查看社区内容、处理举报
- 中英文切换

---

## 3. 三端联动说明

系统重点做了用户端、教练端、管理员端之间的数据联动：

```text
用户端预约课程
  -> 教练端课程人数同步变化
  -> 管理员端课程数据同步变化

用户端取消预约
  -> 教练端/管理员端课程人数同步减少

教练端预约场地
  -> 对应场地时段从可预约列表中移除
  -> 教练端上架课程只能选择本人已预约的场地时段

教练端删除未使用的场地预约
  -> 对应场地时段恢复为可预约状态

教练端上架课程
  -> 用户端课程预约中心显示该课程
  -> 管理员端课程管理显示该课程

用户端投诉/评价
  -> 教练端 Coach Mailbox 可查看与处理

管理员端修改课程
  -> 用户端和教练端课程信息同步变化

用户端打卡、兑换商品、购买 VIP
  -> 用户积分与会员状态动态变化

管理员端新增/编辑/删除积分商城商品
  -> 用户端积分商城商品列表、库存、积分价格和中英文名称同步变化
```

---

## 4. 本地数据库配置

数据库配置位于：

```text
src/main/resources/application.yml
```

默认配置：

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gym_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&characterEncoding=utf8&useUnicode=true&allowPublicKeyRetrieval=true
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:lnj031212}
```

含义：

```text
数据库地址：localhost:3306
数据库名称：gym_db
默认账号：root
默认密码：lnj031212
```

如果你的 MySQL 密码不是 `lnj031212`，可以在启动后端前临时设置：

```cmd
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=你的MySQL密码
```

如果你之前在同一个 CMD 中设置过旧密码，例如：

```cmd
set MYSQL_PASSWORD=zl123456
```

请关闭 CMD 重新打开，或者重新设置为你的当前密码。

---

## 5. 第一次运行前创建数据库

打开 CMD，输入：

```cmd
mysql -u root -p
```

输入你的 MySQL 密码后执行：

```sql
CREATE DATABASE IF NOT EXISTS gym_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

后端连接参数中包含 `createDatabaseIfNotExist=true`，数据库不存在时通常也可以自动创建，但第一次建议手动创建，方便确认 MySQL 账号和密码是否正确。

---

## 6. 启动后端

在项目根目录，也就是包含 `pom.xml` 的目录执行：

```cmd
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

如果电脑没有全局 Maven，可以尝试：

```cmd
mvnw.cmd spring-boot:run
```

---

## 7. 启动前端

另开一个 CMD：

```cmd
cd frontend
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:3000
```

Vite 会将 `/api` 请求代理到后端：

```text
http://localhost:8080
```

本项目推荐使用：

```text
后端：http://localhost:8080
前端：http://localhost:3000
```

也就是后端和前端分别启动。

---

## 8. 访问入口

| 端口 | 地址 |
|---|---|
| 用户端 | http://localhost:3000/user/login |
| 教练端 | http://localhost:3000/coach/login |
| 管理员端 | http://localhost:3000/admin/login |

---

## 9. 测试账号

### 用户端账号

```text
fitness_pro / 123456
gym_master / 123456
yoga_lover / 123456
```

### 教练端账号

```text
coach_1 / 123456
coach_2 / 123456
coach_3 / 123456
coach_4 / 123456
```

### 管理员端账号

```text
admin / admin123
```

说明：后端启动时会检查演示登录账号，确保 `fitness_pro`、`gym_master`、`yoga_lover` 的 `role` 为 `USER`，密码为 `123456`，避免管理员测试时误改登录身份导致无法登录。

---

## 10. 数据初始化说明

后端启动时会检查基础数据是否存在：

- 如果数据库为空，会初始化演示用户、教练、管理员、课程、场地、积分商城商品、社区帖子等数据。
- 如果数据库已有数据，默认不会强制清空。
- 场地基础数据会自动补齐，避免场地预约页面缺少区域。
- 积分商城商品会自动补齐英文名称和英文描述。
- 旧版普通商品会自动同步到积分商城商品表，保证管理员端商品管理和用户端积分商城使用同一套数据。

如果你需要重新生成演示数据，可以在启动后端前设置：

```cmd
set RESET_DEMO_DATA=true
mvn spring-boot:run
```

平时不要设置 `RESET_DEMO_DATA=true`，否则管理员端新增、编辑、删除的数据可能被重置。

---

## 11. 主要目录结构

```text
Gym User Service Platform-final/
├── pom.xml
├── build.gradle
├── README.md
├── scripts/
│   └── repair_demo_user_login.sql
├── src/main/java/com/gym/service/
│   ├── config/          # 安全配置、初始化数据、跨域配置
│   ├── controller/      # 后端接口
│   ├── entity/          # 数据库实体
│   ├── repository/      # JPA 数据访问层
│   ├── service/         # 业务逻辑层
│   └── util/            # JWT 工具类
├── src/main/resources/
│   ├── application.yml  # 本地数据库与后端端口配置
│   └── application.properties
└── frontend/
    ├── package.json
    ├── package-lock.json
    ├── vite.config.js
    └── src/
        ├── router/      # 前端路由
        ├── i18n/        # 中英文语言包
        ├── services/    # 前端 API 请求封装
        ├── config/      # 用户端导航配置
        ├── components/  # 公共组件
        └── views/       # 用户端、教练端、管理员端页面
```

---

## 12. 常见问题

### 12.1 MySQL 密码无法登录

如果执行：

```cmd
mysql -u root -p
```

输入 `lnj031212` 无法登录，但输入旧密码可以登录，说明你电脑上的 MySQL root 密码还没有改。可以先用旧密码进入 MySQL，再执行：

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'lnj031212';
FLUSH PRIVILEGES;
```

### 12.2 后端启动失败，提示数据库连接错误

检查：

```text
1. MySQL 服务是否已经启动
2. root 密码是否和 application.yml 或 MYSQL_PASSWORD 一致
3. gym_db 数据库是否存在
4. 是否在同一个 CMD 中残留了旧的 MYSQL_PASSWORD 环境变量
```

如果设置过旧环境变量，可以关闭 CMD 重新打开，或者重新设置：

```cmd
set MYSQL_PASSWORD=lnj031212
```

### 12.3 前端页面显示旧内容

浏览器可能缓存旧页面。启动新版后建议按：

```text
Ctrl + F5
```

### 12.4 只启动后端访问 8080 看不到新版前端

当前项目推荐用前后端分离方式运行：

```text
后端：http://localhost:8080
前端：http://localhost:3000
```

如果只启动后端并访问 `http://localhost:8080`，可能看不到最新前端页面，因为当前源码包没有使用 `src/main/resources/static` 作为主要前端入口。

### 12.5 用户账号突然无法登录

如果管理员端测试时误把用户的 `role` 改成了 `VIP` 或其他值，用户端登录可能失败。正确规则是：

```text
users.role = 登录身份，只能是 USER / COACH / ADMIN
membership = 会员等级，例如 VIP / 普通会员
```

可以执行：

```sql
USE gym_db;
UPDATE users
SET role = 'USER', active = 1, password = '123456'
WHERE username IN ('fitness_pro', 'gym_master', 'yoga_lover');
```

项目启动后也会自动检查并修复这三个演示用户账号。

---

## 13. 本版本最终检查记录

本版本在打包前完成了以下检查和收尾修复：

```text
1. 检查前端相对路径 import，未发现缺失引用。
2. 检查 zh.json / en.json，JSON 语法正常。
3. 检查 Vue 与 JS 脚本语法，未发现语法错误。
4. 检查 Java 源码括号结构，未发现明显结构错误。
5. 确认数据库仍然连接本地 localhost:3306/gym_db。
6. 确认默认数据库密码为 lnj031212。
7. 确认用户端、教练端、管理员端主要路由入口存在。
8. 确认场地预约、删除预约、上架课程场地下拉框之间的字段已对齐。
9. 确认用户端 VIP 支付页和首页入口保留。
10. 确认管理员端商品管理使用积分商城商品表，能影响用户端积分商城。
11. 修复旧版普通商品同步到积分商城后缺少英文名称/英文描述的问题。
12. 对齐 application.properties 和 application.yml 中的 JWT 配置，避免重复配置造成理解混乱。
```

说明：当前检查环境无法联网下载 Gradle，也没有全局 Maven，因此完整后端编译请在你的本地电脑执行：

```cmd
mvn spring-boot:run
```

前端完整运行请在本地执行：

```cmd
cd frontend
npm install
npm run dev
```

---

## 14. 备注

本项目是本地演示与毕业设计项目版本，支付功能为模拟支付，不会发起真实扣款。项目的商城、VIP、打卡、预约、社区等数据都会写入本地 MySQL 数据库。

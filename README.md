# 基于 SpringBoot 的健身房用户服务平台的设计与实现

v49 为 MySQL 动态持久化 + 丰富演示数据版，技术结构保持为：

- 后端：Spring Boot 2.7 + Java 8 + JDBC + MySQL
- 前端：Vue 3 + Vite
- 开发运行：后端 `mvn spring-boot:run`，前端 `npm run dev`
- 生产运行：`mvn clean package -DskipTests` 后 `java -jar target/gym-user-service-platform-mysql-dynamic-1.0.0.jar`

## 一、MySQL 配置

默认连接：

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/fitlife_service?createDatabaseIfNotExist=true...
spring.datasource.username=root
spring.datasource.password=lnj031212
```

如果你的 MySQL 密码不是 `lnj031212`，请修改：

```text
src/main/resources/application.properties
```

第一次使用或需要重置数据库结构时执行：

```bash
mysql -uroot -p < database/fitlife_mysql.sql
```

如果数据库已经存在并且 `/api/health` 显示 `storage: MySQL`，后续启动通常不需要重复执行 SQL。

## 二、开发模式运行

后端：

```bash
mvn spring-boot:run
```

前端：

```bash
cd frontend
npm install
npm run dev
```

访问：

```text
http://localhost:3000
```

Vite 已配置代理：

```text
/api -> http://127.0.0.1:8080
```

前端也增加了 3000 到 8080 的直连兜底，频繁切换用户端 / 管理端 / 教练端后，登录按钮不会一直卡在“登录中”。

## 三、默认测试账号

用户端测试会员账号，密码均为 `123456`：

```text
fitness_pro
strength_member
yoga_member
running_king
dance_queen
fit_newbie
```

其他端口：

```text
管理员端：admin / admin123
教练端：coach_1 / 123456
教练端：coach_2 / 123456
教练端：coach_3 / 123456
```

## 四、本版主要修复与优化

- 修复频繁切换端口后登录卡住的问题：登录请求 7 秒超时、失败后自动释放按钮、开发模式直连 8080 兜底。
- 用户端登录页测试账号增加到 3 个，并同步更新初始化用户邮箱。
- 用户端首页 Community 不再空白：后端初始化社区数据，前端也提供兜底展示。
- 教练端首页保留 v6 风格 Dashboard：只显示统计卡片和 Today's Schedule，个人信息 / Gym crowd count / Overview 留在 My Profile。
- 教练端预约场地使用时段下方确认按钮，不再使用浏览器顶部 confirm 弹窗。
- 私教课弹窗增加兜底课程提示，避免出现空白。
- 管理端、用户端、教练端继续共用 MySQL 动态状态。


## 五、v49 丰富演示数据

本版本为了让答辩、演示和测试更完整，已将初始化数据扩展为：

- 12 个会员、6 个教练；
- 40 条课程：28 条团课 + 12 条私教课；
- 29 条课程预约记录；
- 15 个场地 / 设施，6 条教练场地预约；
- 18 个积分商城商品，5 条兑换记录；
- 7 条社区动态、6 个组队小队和多条评论；
- 多条投诉、咨询和教练评价。

持久化 state key 已更新为 `fitlife-v49-rich-database-state`，首次启动 v49 会自动写入新数据。

## 六、动态数据说明

主要业务数据来自后端接口并持久化到 MySQL：

- 用户、会员、VIP 到期日
- 教练、课程、预约、取消预约
- 积分、积分商品、兑换记录、库存和销量
- 社区帖子、回复、点赞、置顶、组队
- 打卡记录、连续打卡天数、徽章状态
- 投诉建议、教练评价、教练课程管理

`fitlife_state` 保存完整业务状态，以下可读表用于查看主要动态数据：

```text
fitlife_users
fitlife_coaches
fitlife_memberships
fitlife_classes
fitlife_products
fitlife_point_products
fitlife_bookings
fitlife_posts
fitlife_teams
fitlife_points
fitlife_exchanges
fitlife_facilities
fitlife_facility_bookings
fitlife_complaints
fitlife_reviews
```

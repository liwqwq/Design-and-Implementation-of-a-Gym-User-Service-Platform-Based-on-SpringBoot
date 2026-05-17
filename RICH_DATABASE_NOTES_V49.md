# v49 数据丰富记录

本次基于 v48 bugfix-stable 继续优化，重点丰富项目演示数据库，让用户端、管理员端、教练端打开后都有更真实的业务数据。

## 已完成

1. 更换持久化 state key 为 `fitlife-v49-rich-database-state`，避免旧版本空数据污染新版本。
2. 丰富会员数据：从 8 个会员扩展为 12 个会员，并补充邮箱、手机号、生日、积分、会员资料、训练偏好说明。
3. 丰富教练数据：从 4 个教练扩展为 6 个教练，覆盖力量、瑜伽、减脂单车、搏击、游泳康复、舞蹈有氧。
4. 丰富课程数据：扩展为 40 条课程，包括 28 条一周团课排期和 12 条私教课。
5. 丰富预约数据：新增 29 条预约记录，保证课程页、我的预约、教练学生列表、教练首页统计都有内容。
6. 丰富场地数据：扩展为 15 个场地，并预置 6 条教练场地预约。
7. 丰富积分商城：扩展为 18 个积分商品，增加优惠券、装备、服务、VIP 专属、预约权益。
8. 丰富兑换记录：预置 5 条兑换记录，让用户端兑换记录区域不再空白。
9. 丰富社区：新增广告活动、7 条帖子、评论、6 个组队小队、举报记录。
10. 丰富教练邮箱：新增投诉、咨询和评价记录，方便测试 Handle / Reply 功能。
11. 扩展 MySQL 可读业务表：新增商品、场地、场地预约、投诉、评价等同步表。

## 检查结果

已完成 Java 语法级编译检查：

```bash
javac -encoding UTF-8 -cp /mnt/data/javastubs/classes -d /tmp/v49classes \
  src/main/java/com/fitlife/platform/store/VisualStore.java \
  src/main/java/com/fitlife/platform/controller/ApiController.java
```

并通过反射初始化数据后检查数量：

- users = 12
- coaches = 6
- classes = 40
- bookings = 29
- point products = 18
- posts = 7
- teams = 6
- exchanges = 5
- facilities = 15
- facility bookings = 6

沙盒环境没有 Maven，因此 jar 仍需要在本地执行：

```bash
mvn clean package -DskipTests
```

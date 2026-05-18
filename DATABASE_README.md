# 数据库说明

数据库名称：`fitlife_service`

初始化：

```bash
mysql -uroot -p < database/fitlife_mysql.sql
```

## v49 数据丰富说明

本版本将 Spring Boot 持久化状态 key 更新为：

```text
fitlife-v49-rich-database-state
```

所以首次启动 v49 时，会自动写入一套更完整的演示数据，不会继续复用旧版本里较空的 `fitlife-v40-mall-class-stable-state` 数据。

已丰富的数据包括：

- 12 个会员账号，包含积分、会员类型、打卡记录、资料信息。
- 6 个教练账号，覆盖力量、瑜伽、单车、搏击、游泳、舞蹈等方向。
- 40 条课程数据：28 条团课 + 12 条私教课，覆盖一周排课。
- 29 条课程预约记录，保证用户端、教练端学生列表、教练首页统计都有数据。
- 15 个场地 / 设施，6 条教练场地预约记录。
- 18 个积分商城商品，包含优惠券、运动装备、会员服务、VIP 专属商品。
- 5 条积分兑换记录，用户端“我的兑换记录”不再为空。
- 7 条社区动态、6 个组队小队、评论、举报与广告活动数据。
- 教练邮箱中增加投诉、咨询与课程评价数据。

## 可检查动态变化的表

```sql
SELECT * FROM fitlife_users;
SELECT * FROM fitlife_coaches;
SELECT * FROM fitlife_memberships;
SELECT * FROM fitlife_classes;
SELECT * FROM fitlife_bookings;
SELECT * FROM fitlife_products;
SELECT * FROM fitlife_point_products;
SELECT * FROM fitlife_exchanges;
SELECT * FROM fitlife_posts;
SELECT * FROM fitlife_teams;
SELECT * FROM fitlife_points;
SELECT * FROM fitlife_facilities;
SELECT * FROM fitlife_facility_bookings;
SELECT * FROM fitlife_complaints;
SELECT * FROM fitlife_reviews;
```

例如：

- 管理员端新增课程后，`fitlife_classes` 会新增记录，用户端课程预约页同步显示。
- 用户端预约课程后，`fitlife_bookings` 会新增记录，`fitlife_classes.booked_count` 会变化。
- 用户端兑换积分商品后，`fitlife_point_products.stock_quantity` 会减少，`sold_count` 会增加，`fitlife_points` 会扣减积分，`fitlife_exchanges` 会新增记录。
- 教练端预约场地后，`fitlife_facility_bookings` 会新增记录。

## 测试账号

用户端账号密码均为：`123456`

- `fitness_pro`
- `strength_member`
- `yoga_member`
- `running_king`
- `dance_queen`
- `fit_newbie`

教练端账号密码均为：`123456`

- `coach_1`
- `coach_2`
- `coach_3`
- `coach_4`
- `coach_5`
- `coach_6`

管理员端：

- 用户名：`admin`
- 密码：`admin123`

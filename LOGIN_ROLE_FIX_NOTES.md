# Login Role Fix Notes

本次修复解决了普通用户购买 VIP 后无法登录的问题。

## 原因

旧逻辑把 `users.role` 改成了 `VIP`。但 `users.role` 是登录身份字段，只应保存：

- `USER`
- `COACH`
- `ADMIN`

VIP、普通会员、月卡、年卡等应保存到 `membership.membership_type`。

## 修复内容

1. 用户购买 VIP 后，`users.role` 仍保持 `USER`。
2. 登录逻辑兼容旧数据库中已经错误写入的 `VIP`，登录成功后会自动修正为 `USER`。
3. 增加 `scripts/repair_demo_user_login.sql`，用于一键修复三个用户端测试账号密码和 role。脚本会先把密码恢复为明文 `123456`，项目登录成功后会自动升级为 BCrypt 加密密码。

## 手动修复 SQL

如果本地旧数据库已被改乱，可进入 MySQL 后执行：

```sql
source scripts/repair_demo_user_login.sql;
```

或复制脚本内 SQL 直接执行。

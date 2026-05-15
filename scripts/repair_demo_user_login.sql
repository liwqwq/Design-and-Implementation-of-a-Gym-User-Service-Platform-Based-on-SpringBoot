USE gym_db;

-- 修复用户端登录页展示的三个测试账号。
-- role 表示登录身份，必须是 USER；VIP 等级应写入 membership 表，不应写入 users.role。
-- 这里先把密码恢复为明文 123456；项目登录成功后会自动升级为 BCrypt 加密密码。
UPDATE users
SET role = 'USER', active = 1, password = '123456'
WHERE username IN ('fitness_pro', 'gym_master', 'yoga_lover');

SELECT username, role, active, password
FROM users
WHERE username IN ('fitness_pro', 'gym_master', 'yoga_lover');

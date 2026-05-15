-- FitLife / gym-service — MySQL 初始化参考
-- 1) 确保本地 MySQL 已启动，且账号有建库权限。
-- 2) 应用使用 spring.jpa.hibernate.ddl-auto=update 时会自动建/更新表结构（含 posts.category 等字段）。
-- 3) 以下为可选：手动建库与索引（在 Hibernate 已生成表后执行索引语句）。

CREATE DATABASE IF NOT EXISTS gym_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE gym_db;

-- 可选索引（若已存在同名索引会报错，忽略即可）
-- CREATE INDEX idx_posts_active_created ON posts (active, created_at);
-- CREATE INDEX idx_posts_active_category ON posts (active, category);

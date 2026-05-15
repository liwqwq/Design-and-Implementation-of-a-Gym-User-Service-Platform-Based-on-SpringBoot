-- 将历史帖子中空的 category 视为「分享」，与后端 COALESCE(category,'share') 一致
USE gym_db;
UPDATE posts SET category = 'share' WHERE category IS NULL OR TRIM(category) = '';

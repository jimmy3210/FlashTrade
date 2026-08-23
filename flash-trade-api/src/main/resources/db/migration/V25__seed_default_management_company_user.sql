-- ===================================================
-- V25: 默认管理公司与管理员用户（用于本地开发登录）
-- ===================================================

INSERT INTO t_company (name, is_active)
SELECT 'Default Management Company', 1
WHERE NOT EXISTS (
    SELECT 1 FROM t_company WHERE name = 'Default Management Company'
);

SET @company_id = (
    SELECT id FROM t_company WHERE name = 'Default Management Company' LIMIT 1
);

INSERT INTO t_group (company_id, name, is_active)
SELECT @company_id, 'Management Group', 1
WHERE NOT EXISTS (
    SELECT 1 FROM t_group WHERE company_id = @company_id AND name = 'Management Group'
);

INSERT INTO users (username, password_hash, role, enabled)
SELECT 'admin', '$2b$12$Bq1kPAfI6MVuSkerk7Qx/OsxQOEhTcRrxX7G5MxpDw8vpDZmk9qnm', 'ROLE_ADMIN', 1
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

-- 说明：
-- 1. 该用户不绑定券商账户，适合前端本地登录与系统功能验证
-- 2. 默认密码为 admin123
-- 3. 若需要重新初始化，可删除该条记录后由 Flyway 重新执行该 migration

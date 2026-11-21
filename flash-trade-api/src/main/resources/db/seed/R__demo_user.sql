-- ===================================================
-- Repeatable seed: 插入默认管理公司与管理员用户（本地登录用）
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
SELECT 'ft_x7pL2mK9', '$2b$12$nU8udV8VODIgiE/WYeoEbOaCa63GksEtyuBgwQLkofEQygha1pJJS', 'ROLE_ADMIN', 1
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'ft_x7pL2mK9'
);

UPDATE users
SET password_hash = '$2b$12$nU8udV8VODIgiE/WYeoEbOaCa63GksEtyuBgwQLkofEQygha1pJJS',
    role = 'ROLE_ADMIN',
    enabled = 1,
    updated_at = NOW()
WHERE username = 'ft_x7pL2mK9';

UPDATE users
SET enabled = 0,
    updated_at = NOW()
WHERE username = 'admin';

-- 说明：
-- 1. 本地开发默认登录使用 ft_x7pL2mK9 和部署者在隔离环境中配置的本地密码
-- 2. 不绑定券商账户，便于连通前端并完成系统功能验证
-- 3. 若需要做真实业务租户，后续可在管理端再创建对应公司/用户

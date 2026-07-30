-- ===================================================
-- Repeatable seed: 插入可登录的 demo 用户及示例券商账户
-- ===================================================

INSERT INTO t_company (name, is_active)
SELECT 'Demo Trading Company', 1
WHERE NOT EXISTS (SELECT 1 FROM t_company WHERE name = 'Demo Trading Company');

SET @demo_company_id = (
    SELECT id FROM t_company WHERE name = 'Demo Trading Company' LIMIT 1
);

INSERT INTO t_group (company_id, name, is_active)
SELECT @demo_company_id, 'Demo Trading Group', 1
WHERE NOT EXISTS (SELECT 1 FROM t_group WHERE company_id = @demo_company_id AND name = 'Demo Trading Group');

INSERT INTO users (username, password_hash, role, enabled)
SELECT 'demo', '$2b$12$XRu32Owi6r4OWasIEeQoouzrn4h1z9CgeMpkMK2Dd5rdLTY399PD6', 'ROLE_TRADER', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'demo');

SET @demo_user_id = (
    SELECT id FROM users WHERE username = 'demo' LIMIT 1
);

INSERT INTO t_broker_account (user_id, broker_id, account_no, account_type, status)
SELECT @demo_user_id,
       (SELECT id FROM t_broker WHERE broker_code = 'schwab' LIMIT 1),
       'DEMO-ACC-001',
       'CASH',
       'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM t_broker_account
    WHERE user_id = @demo_user_id AND account_no = 'DEMO-ACC-001'
);

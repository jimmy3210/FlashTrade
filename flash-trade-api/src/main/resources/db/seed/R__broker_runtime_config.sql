-- ===================================================
-- Repeatable seed: 初始化券商运行时模式与 API URL
-- ===================================================

INSERT INTO t_broker_runtime_config (broker_name, mode, base_url, mock_enabled)
VALUES
    ('ETRADE', 'MOCK', 'http://localhost:8090/mock/etrade', 1),
    ('SCHWAB', 'MOCK', 'http://localhost:8090/mock/schwab', 1),
    ('TASTYTRADE', 'MOCK', 'http://localhost:8090/mock/tastytrade', 1),
    ('TASTYWORKS', 'MOCK', 'http://localhost:8090/mock/tastyworks', 1)
ON DUPLICATE KEY UPDATE
    mode = VALUES(mode),
    base_url = VALUES(base_url),
    mock_enabled = VALUES(mock_enabled);

-- ===================================================
-- V14: 插入 demo 用户持仓、订单、成交记录和券商账户权限
-- ===================================================

SET @demo_user_id = (SELECT id FROM users WHERE username = 'demo' LIMIT 1);
SET @schwab_broker_id = (SELECT id FROM t_broker WHERE broker_code = 'schwab' LIMIT 1);
SET @demo_broker_account_id = (SELECT id FROM t_broker_account WHERE user_id = @demo_user_id AND broker_id = @schwab_broker_id LIMIT 1);

INSERT INTO positions (
    user_id,
    account_id,
    symbol,
    quantity,
    average_cost,
    current_price,
    market_value,
    unrealized_pnl,
    broker_name,
    updated_at
)
VALUES (
    @demo_user_id,
    'DEMO-ACC-001',
    'AAPL',
    100.0000,
    150.0000,
    172.5000,
    17250.0000,
    2250.0000,
    'schwab',
    NOW()
);

INSERT INTO orders (
    user_id,
    account_id,
    symbol,
    side,
    quantity,
    price,
    status,
    broker_name,
    external_order_id,
    created_at,
    filled_at,
    updated_at
)
VALUES (
    @demo_user_id,
    'DEMO-ACC-001',
    'AAPL',
    'BUY',
    100.0000,
    150.0000,
    'FILLED',
    'schwab',
    'DEMO-ORDER-001',
    NOW(),
    NOW(),
    NOW()
);

SET @demo_order_id = LAST_INSERT_ID();

INSERT INTO trades (
    order_id,
    user_id,
    account_id,
    symbol,
    side,
    quantity,
    price,
    commission,
    broker_name,
    external_trade_id,
    traded_at,
    created_at
)
VALUES (
    @demo_order_id,
    @demo_user_id,
    'DEMO-ACC-001',
    'AAPL',
    'BUY',
    100.0000,
    150.0000,
    1.50,
    'schwab',
    'DEMO-TRADE-001',
    NOW(),
    NOW()
);

INSERT INTO t_broker_account_permission (
    account_id,
    permission,
    granted_at
)
VALUES (
    @demo_broker_account_id,
    'TRADE',
    NOW()
);

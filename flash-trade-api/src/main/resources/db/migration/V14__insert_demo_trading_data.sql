-- ===================================================
-- V14: 插入 demo 用户持仓、订单、成交记录和券商账户权限
-- ===================================================

-- 插入演示持仓（如果不存在）
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
SELECT
    u.id,
    'DEMO-ACC-001',
    'AAPL',
    100.0000,
    150.0000,
    172.5000,
    17250.0000,
    2250.0000,
    'schwab',
    NOW()
FROM users u
WHERE u.username = 'demo'
  AND NOT EXISTS (
      SELECT 1
      FROM positions p
      WHERE p.user_id = u.id
        AND p.account_id = 'DEMO-ACC-001'
        AND p.symbol = 'AAPL'
  );

-- 插入演示订单（如果不存在）
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
SELECT
    u.id,
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
FROM users u
WHERE u.username = 'demo'
  AND NOT EXISTS (
      SELECT 1
      FROM orders o
      WHERE o.user_id = u.id
        AND o.external_order_id = 'DEMO-ORDER-001'
  );

-- 插入演示成交（如果不存在）
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
SELECT
    o.id,
    u.id,
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
FROM users u
JOIN orders o ON o.user_id = u.id
WHERE u.username = 'demo'
  AND o.external_order_id = 'DEMO-ORDER-001'
  AND NOT EXISTS (
      SELECT 1
      FROM trades t
      WHERE t.user_id = u.id
        AND t.external_trade_id = 'DEMO-TRADE-001'
  );

-- 插入券商账户权限（如果不存在）
INSERT INTO t_broker_account_permission (
    account_id,
    permission,
    granted_at
)
SELECT
    ba.id,
    'TRADE',
    NOW()
FROM users u
JOIN t_broker_account ba
  ON ba.user_id = u.id
JOIN t_broker b
  ON b.id = ba.broker_id
WHERE u.username = 'demo'
  AND b.broker_code = 'schwab'
  AND ba.account_no = 'DEMO-ACC-001'
  AND NOT EXISTS (
      SELECT 1
      FROM t_broker_account_permission p
      WHERE p.account_id = ba.id
        AND p.permission = 'TRADE'
  );

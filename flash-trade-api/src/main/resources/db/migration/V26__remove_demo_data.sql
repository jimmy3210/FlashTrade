-- ===================================================
-- V26: Remove legacy demo user/company and related demo data
-- ===================================================

-- This migration deletes demo data inserted by older demo migrations.
-- It's written idempotently: it only removes rows that match demo identifiers.

-- Remove demo trades, orders, positions, broker account permissions and accounts, then the user and company

-- Get demo user id (if exists)
SET @demo_user_id = (SELECT id FROM users WHERE username = 'demo' LIMIT 1);

-- Delete trades for demo user
DELETE FROM trades WHERE user_id = @demo_user_id;

-- Delete orders for demo user
DELETE FROM orders WHERE user_id = @demo_user_id;

-- Delete positions for demo user
DELETE FROM positions WHERE user_id = @demo_user_id;

-- Delete broker account permissions for demo accounts
DELETE FROM t_broker_account_permission
WHERE account_id IN (
    SELECT id FROM t_broker_account WHERE user_id = @demo_user_id
);

-- Delete broker accounts for demo user
DELETE FROM t_broker_account WHERE user_id = @demo_user_id;

-- Finally delete the demo user
DELETE FROM users WHERE username = 'demo';

-- Remove Demo company and its groups (if present)
SET @demo_company_id = (SELECT id FROM t_company WHERE name = 'Demo Trading Company' LIMIT 1);

-- Delete groups under demo company
DELETE FROM t_group WHERE company_id = @demo_company_id;

-- Delete the company
DELETE FROM t_company WHERE id = @demo_company_id;

-- Note: this migration intentionally does not drop legacy migration files; it merely removes data
-- inserted by them so new deployments won't carry demo artifacts.

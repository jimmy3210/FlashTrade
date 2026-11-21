-- Add explicit company ownership and a stable account-scope key.
-- Existing users and string-based scopes are intentionally left unmapped;
-- authorization must deny them until an operator performs a verified mapping.
ALTER TABLE users
    ADD COLUMN company_id BIGINT NULL,
    ADD INDEX idx_users_company_id (company_id),
    ADD CONSTRAINT fk_users_company
        FOREIGN KEY (company_id) REFERENCES t_company (id);

ALTER TABLE t_user_scope
    ADD COLUMN target_broker_account_id BIGINT NULL,
    ADD INDEX idx_user_scope_broker_account (target_broker_account_id),
    ADD CONSTRAINT fk_scope_broker_account
        FOREIGN KEY (target_broker_account_id) REFERENCES t_broker_account (id);

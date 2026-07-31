-- 改造收付款表：bank_account 改为 company_account_id（外键→t_company_account）
ALTER TABLE t_fin_payment ADD COLUMN company_account_id BIGINT DEFAULT NULL COMMENT '公司账户ID' AFTER amount;
-- 迁移现有数据（如果有 bank_account 值，留空，人工补录）
-- ALTER TABLE t_fin_payment DROP COLUMN bank_account;  -- 先保留兼容，后续删除

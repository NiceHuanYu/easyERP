-- 移除客户/供应商表中的银行字段（已迁移到 t_counterparty_account）
ALTER TABLE t_base_customer DROP COLUMN bank_name;
ALTER TABLE t_base_customer DROP COLUMN bank_account;
ALTER TABLE t_base_customer DROP COLUMN bank_account_name;

ALTER TABLE t_base_supplier DROP COLUMN bank_name;
ALTER TABLE t_base_supplier DROP COLUMN bank_account;
ALTER TABLE t_base_supplier DROP COLUMN bank_account_name;

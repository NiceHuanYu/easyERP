-- 扩展客户表，增加银行信息字段
ALTER TABLE t_base_customer ADD COLUMN bank_name         VARCHAR(100) DEFAULT NULL COMMENT '开户行';
ALTER TABLE t_base_customer ADD COLUMN bank_account      VARCHAR(50)  DEFAULT NULL COMMENT '银行账号';
ALTER TABLE t_base_customer ADD COLUMN bank_account_name VARCHAR(100) DEFAULT NULL COMMENT '户名';

-- 扩展供应商表，增加银行信息字段
ALTER TABLE t_base_supplier ADD COLUMN bank_name         VARCHAR(100) DEFAULT NULL COMMENT '开户行';
ALTER TABLE t_base_supplier ADD COLUMN bank_account      VARCHAR(50)  DEFAULT NULL COMMENT '银行账号';
ALTER TABLE t_base_supplier ADD COLUMN bank_account_name VARCHAR(100) DEFAULT NULL COMMENT '户名';

-- 补充 t_company_account 和 t_counterparty_account 缺少的 BaseEntity 字段
ALTER TABLE t_company_account ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人';
ALTER TABLE t_company_account ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人';

ALTER TABLE t_counterparty_account ADD COLUMN create_by BIGINT DEFAULT NULL COMMENT '创建人';
ALTER TABLE t_counterparty_account ADD COLUMN update_by BIGINT DEFAULT NULL COMMENT '更新人';

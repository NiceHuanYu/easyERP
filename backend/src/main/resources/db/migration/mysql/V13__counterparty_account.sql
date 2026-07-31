-- 客户/供应商银行账户（支持多账户）
CREATE TABLE t_counterparty_account (
    id            BIGINT        NOT NULL PRIMARY KEY COMMENT '主键',
    owner_type    VARCHAR(20)   NOT NULL COMMENT 'CUSTOMER / SUPPLIER',
    owner_id      BIGINT        NOT NULL COMMENT '客户/供应商ID',
    bank_name     VARCHAR(100)  DEFAULT NULL COMMENT '银行名称',
    branch_name   VARCHAR(100)  DEFAULT NULL COMMENT '支行',
    account_no    VARCHAR(50)   DEFAULT NULL COMMENT '账号',
    account_name  VARCHAR(100)  DEFAULT NULL COMMENT '户名',
    currency      VARCHAR(10)   DEFAULT 'CNY' COMMENT '币种',
    account_type  VARCHAR(20)   DEFAULT 'BOTH' COMMENT 'RECEIVE / PAY / BOTH',
    status        INT           DEFAULT 1 COMMENT '1启用 0禁用',
    remark        VARCHAR(255)  DEFAULT NULL,
    create_time   DATETIME      DEFAULT NULL,
    update_time   DATETIME      DEFAULT NULL
);

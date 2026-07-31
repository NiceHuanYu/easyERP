CREATE TABLE t_company_account (
    id            BIGINT        NOT NULL PRIMARY KEY COMMENT '主键',
    bank_name     VARCHAR(100)  DEFAULT NULL COMMENT '银行名称',
    branch_name   VARCHAR(100)  DEFAULT NULL COMMENT '支行名称',
    account_no    VARCHAR(50)   DEFAULT NULL COMMENT '账号',
    account_name  VARCHAR(100)  DEFAULT NULL COMMENT '户名',
    currency      VARCHAR(10)   DEFAULT 'CNY' COMMENT '币种',
    account_type  VARCHAR(20)   NOT NULL DEFAULT 'BOTH' COMMENT 'RECEIVE收款/PAY付款/BOTH均可',
    status        INT           DEFAULT 1 COMMENT '1启用 0禁用',
    remark        VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    create_time   DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME      DEFAULT NULL COMMENT '更新时间'
);

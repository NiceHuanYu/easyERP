-- 库存调拨单
CREATE TABLE t_inv_transfer (
    id            BIGINT        NOT NULL PRIMARY KEY,
    transfer_no   VARCHAR(50)   NOT NULL COMMENT '调拨单号',
    from_warehouse_id BIGINT    NOT NULL COMMENT '调出仓库ID',
    to_warehouse_id   BIGINT    NOT NULL COMMENT '调入仓库ID',
    transfer_date DATE          DEFAULT NULL COMMENT '调拨日期',
    status        VARCHAR(20)   DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED',
    remark        VARCHAR(500)  DEFAULT NULL,
    create_time   DATETIME      DEFAULT NULL,
    update_time   DATETIME      DEFAULT NULL,
    create_by     BIGINT        DEFAULT NULL,
    update_by     BIGINT        DEFAULT NULL
);

-- 库存调拨明细
CREATE TABLE t_inv_transfer_item (
    id            BIGINT        NOT NULL PRIMARY KEY,
    transfer_id   BIGINT        NOT NULL COMMENT '调拨单ID',
    material_id   BIGINT        NOT NULL COMMENT '物料ID',
    quantity      DECIMAL(18,4) NOT NULL COMMENT '调拨数量',
    create_time   DATETIME      DEFAULT NULL,
    update_time   DATETIME      DEFAULT NULL,
    create_by     BIGINT        DEFAULT NULL,
    update_by     BIGINT        DEFAULT NULL
);

-- =====================================================
-- V2__erp_core_tables.sql — 生产 / 采购 / 库存 / 财务
-- =====================================================

-- ==================== 生产管理 ====================

CREATE TABLE t_prod_order (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '工单ID',
    order_no        VARCHAR(30)    NOT NULL COMMENT '工单号 (MO + yyyyMMdd + 流水)',
    sales_order_id  BIGINT         DEFAULT NULL COMMENT '关联销售订单ID',
    material_id     BIGINT         NOT NULL COMMENT '成品物料ID',
    plan_quantity   DECIMAL(18,2)  NOT NULL COMMENT '计划生产数量',
    finish_quantity DECIMAL(18,2)  DEFAULT 0 COMMENT '完工数量',
    start_date      DATE           DEFAULT NULL COMMENT '计划开始日期',
    end_date        DATE           DEFAULT NULL COMMENT '计划结束日期',
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/RELEASED/IN_PROGRESS/FINISHED/COMPLETED',
    remark          VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_sales_order (sales_order_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产工单主表';

CREATE TABLE t_prod_order_bom (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '主键',
    order_id        BIGINT         NOT NULL COMMENT '工单ID',
    material_id     BIGINT         NOT NULL COMMENT '子物料ID',
    required_qty    DECIMAL(18,4)  NOT NULL COMMENT '需求数量',
    picked_qty      DECIMAL(18,4)  DEFAULT 0 COMMENT '已领数量',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单物料需求';

CREATE TABLE t_prod_picking (
    id            BIGINT         NOT NULL PRIMARY KEY COMMENT '领料单ID',
    picking_no    VARCHAR(30)    NOT NULL COMMENT '领料单号',
    order_id      BIGINT         NOT NULL COMMENT '工单ID',
    warehouse_id  BIGINT         NOT NULL COMMENT '仓库ID',
    picking_date  DATE           DEFAULT NULL COMMENT '领料日期',
    status        VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/CONFIRMED',
    remark        VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by     BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by     BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted       TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_picking_no (picking_no),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领料单主表';

CREATE TABLE t_prod_picking_item (
    id            BIGINT         NOT NULL PRIMARY KEY COMMENT '明细ID',
    picking_id    BIGINT         NOT NULL COMMENT '领料单ID',
    material_id   BIGINT         NOT NULL COMMENT '物料ID',
    request_qty   DECIMAL(18,2)  NOT NULL COMMENT '申请数量',
    actual_qty    DECIMAL(18,2)  NOT NULL COMMENT '实发数量',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_picking_id (picking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领料单明细表';

CREATE TABLE t_prod_finish (
    id            BIGINT         NOT NULL PRIMARY KEY COMMENT '完工入库单ID',
    finish_no     VARCHAR(30)    NOT NULL COMMENT '入库单号',
    order_id      BIGINT         NOT NULL COMMENT '工单ID',
    warehouse_id  BIGINT         NOT NULL COMMENT '仓库ID',
    finish_date   DATE           DEFAULT NULL COMMENT '入库日期',
    status        VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/CONFIRMED',
    remark        VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by     BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by     BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted       TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_finish_no (finish_no),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='完工入库单主表';

CREATE TABLE t_prod_finish_item (
    id          BIGINT         NOT NULL PRIMARY KEY COMMENT '明细ID',
    finish_id   BIGINT         NOT NULL COMMENT '入库单ID',
    material_id BIGINT         NOT NULL COMMENT '物料ID',
    quantity    DECIMAL(18,2)  NOT NULL COMMENT '入库数量',
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_finish_id (finish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='完工入库明细表';

-- ==================== 采购管理 ====================

CREATE TABLE t_pur_requisition (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '采购申请ID',
    requisition_no  VARCHAR(30)    NOT NULL COMMENT '申请单号',
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/APPROVED/ORDERED',
    remark          VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_requisition_no (requisition_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请主表';

CREATE TABLE t_pur_requisition_item (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '明细ID',
    requisition_id  BIGINT         NOT NULL COMMENT '申请单ID',
    material_id     BIGINT         NOT NULL COMMENT '物料ID',
    quantity        DECIMAL(18,2)  NOT NULL COMMENT '申请数量',
    ordered_qty     DECIMAL(18,2)  DEFAULT 0 COMMENT '已下单数量',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_requisition_id (requisition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请明细表';

CREATE TABLE t_pur_order (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '采购订单ID',
    order_no        VARCHAR(30)    NOT NULL COMMENT '订单号 (PO + yyyyMMdd + 流水)',
    supplier_id     BIGINT         NOT NULL COMMENT '供应商ID',
    order_date      DATE           NOT NULL COMMENT '订单日期',
    total_amount    DECIMAL(18,2)  DEFAULT 0 COMMENT '总金额',
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/SUBMITTED/APPROVED/PARTIAL_RECEIVED/COMPLETED/CLOSED',
    remark          VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_supplier (supplier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单主表';

CREATE TABLE t_pur_order_item (
    id            BIGINT         NOT NULL PRIMARY KEY COMMENT '明细ID',
    order_id      BIGINT         NOT NULL COMMENT '订单ID',
    line_no       INT            NOT NULL COMMENT '行号',
    material_id   BIGINT         NOT NULL COMMENT '物料ID',
    quantity      DECIMAL(18,2)  NOT NULL COMMENT '数量',
    unit          VARCHAR(20)    NOT NULL COMMENT '单位',
    price         DECIMAL(18,2)  NOT NULL COMMENT '单价',
    amount        DECIMAL(18,2)  NOT NULL COMMENT '金额',
    received_qty  DECIMAL(18,2)  DEFAULT 0 COMMENT '已收货数量',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

CREATE TABLE t_pur_receiving (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '收货单ID',
    receiving_no    VARCHAR(30)    NOT NULL COMMENT '收货单号',
    order_id        BIGINT         NOT NULL COMMENT '采购订单ID',
    warehouse_id    BIGINT         NOT NULL COMMENT '仓库ID',
    receiving_date  DATE           DEFAULT NULL COMMENT '收货日期',
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/CONFIRMED',
    remark          VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_receiving_no (receiving_no),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货单主表';

CREATE TABLE t_pur_receiving_item (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '明细ID',
    receiving_id    BIGINT         NOT NULL COMMENT '收货单ID',
    order_item_id   BIGINT         NOT NULL COMMENT '订单明细ID',
    material_id     BIGINT         NOT NULL COMMENT '物料ID',
    quantity        DECIMAL(18,2)  NOT NULL COMMENT '收货数量',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_receiving_id (receiving_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货单明细表';

-- ==================== 库存管理 ====================

CREATE TABLE t_inv_stock (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '主键',
    material_id     BIGINT         NOT NULL COMMENT '物料ID',
    warehouse_id    BIGINT         NOT NULL COMMENT '仓库ID',
    quantity        DECIMAL(18,4)  NOT NULL DEFAULT 0 COMMENT '库存数量',
    available_qty   DECIMAL(18,4)  NOT NULL DEFAULT 0 COMMENT '可用数量',
    locked_qty      DECIMAL(18,4)  NOT NULL DEFAULT 0 COMMENT '锁定数量',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_material_warehouse (material_id, warehouse_id),
    INDEX idx_material (material_id),
    INDEX idx_warehouse (warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

CREATE TABLE t_inv_transaction (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '流水ID',
    material_id     BIGINT         NOT NULL COMMENT '物料ID',
    warehouse_id    BIGINT         NOT NULL COMMENT '仓库ID',
    type            VARCHAR(20)    NOT NULL COMMENT '变动类型：PURCHASE_IN/PICKING_OUT/PICKING_RETURN/FINISH_IN/SHIPPING_OUT/TRANSFER/ADJUST/SCRAP',
    quantity        DECIMAL(18,4)  NOT NULL COMMENT '变动数量（正=入库，负=出库）',
    current_stock   DECIMAL(18,4)  NOT NULL COMMENT '变动后库存',
    source_no       VARCHAR(30)    DEFAULT NULL COMMENT '来源单号',
    source_type     VARCHAR(30)    DEFAULT NULL COMMENT '来源单据类型',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT         DEFAULT NULL COMMENT '操作人',
    INDEX idx_material_warehouse (material_id, warehouse_id),
    INDEX idx_create_time (create_time),
    INDEX idx_source (source_type, source_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ==================== 财务管理 ====================

CREATE TABLE t_fin_receivable (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '应收ID',
    delivery_id     BIGINT         NOT NULL COMMENT '发货单ID',
    customer_id     BIGINT         NOT NULL COMMENT '客户ID',
    receivable_amount DECIMAL(18,2) NOT NULL COMMENT '应收金额',
    received_amount DECIMAL(18,2) DEFAULT 0 COMMENT '已收金额',
    status          VARCHAR(20)    NOT NULL DEFAULT 'UNPAID' COMMENT '状态：UNPAID/PARTIAL_PAID/PAID',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_customer (customer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应收台账';

CREATE TABLE t_fin_payable (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '应付ID',
    receiving_id    BIGINT         NOT NULL COMMENT '收货单ID',
    supplier_id     BIGINT         NOT NULL COMMENT '供应商ID',
    payable_amount  DECIMAL(18,2)  NOT NULL COMMENT '应付金额',
    paid_amount     DECIMAL(18,2)  DEFAULT 0 COMMENT '已付金额',
    status          VARCHAR(20)    NOT NULL DEFAULT 'UNPAID' COMMENT '状态：UNPAID/PARTIAL_PAID/PAID',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_supplier (supplier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应付台账';

CREATE TABLE t_fin_payment (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '收付款单ID',
    payment_no      VARCHAR(30)    NOT NULL COMMENT '收付款单号',
    type            VARCHAR(10)    NOT NULL COMMENT '类型：RECEIVE/PAY',
    counterparty_id BIGINT         NOT NULL COMMENT '对方ID（客户/供应商）',
    amount          DECIMAL(18,2)  NOT NULL COMMENT '金额',
    bank_account    VARCHAR(50)    DEFAULT NULL COMMENT '银行账号',
    payment_date    DATE           NOT NULL COMMENT '收付款日期',
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/CONFIRMED',
    remark          VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_payment_no (payment_no),
    INDEX idx_counterparty (counterparty_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收付款单';

CREATE TABLE t_fin_payment_item (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '核销明细ID',
    payment_id      BIGINT         NOT NULL COMMENT '收付款单ID',
    receivable_id   BIGINT         DEFAULT NULL COMMENT '应收ID（收款时）',
    payable_id      BIGINT         DEFAULT NULL COMMENT '应付ID（付款时）',
    amount          DECIMAL(18,2)  NOT NULL COMMENT '核销金额',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_payment_id (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收付款核销明细表';

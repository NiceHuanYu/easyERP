-- =====================================================
-- V2__erp_core_tables.sql (PostgreSQL)
-- =====================================================

-- ==================== 生产管理 ====================

CREATE TABLE t_prod_order (
    id              BIGINT         NOT NULL PRIMARY KEY,
    order_no        VARCHAR(30)    NOT NULL,
    sales_order_id  BIGINT         DEFAULT NULL,
    material_id     BIGINT         NOT NULL,
    plan_quantity   DECIMAL(18,2)  NOT NULL,
    finish_quantity DECIMAL(18,2)  DEFAULT 0,
    start_date      DATE           DEFAULT NULL,
    end_date        DATE           DEFAULT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    remark          VARCHAR(500)   DEFAULT NULL,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT         DEFAULT NULL,
    update_by       BIGINT         DEFAULT NULL,
    deleted         SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_order_no UNIQUE (order_no)
);

COMMENT ON TABLE t_prod_order IS '生产工单主表';
COMMENT ON COLUMN t_prod_order.id IS '工单ID';
COMMENT ON COLUMN t_prod_order.order_no IS '工单号 (MO + yyyyMMdd + 流水)';
COMMENT ON COLUMN t_prod_order.sales_order_id IS '关联销售订单ID';
COMMENT ON COLUMN t_prod_order.material_id IS '成品物料ID';
COMMENT ON COLUMN t_prod_order.plan_quantity IS '计划生产数量';
COMMENT ON COLUMN t_prod_order.finish_quantity IS '完工数量';
COMMENT ON COLUMN t_prod_order.start_date IS '计划开始日期';
COMMENT ON COLUMN t_prod_order.end_date IS '计划结束日期';
COMMENT ON COLUMN t_prod_order.status IS '状态：DRAFT/RELEASED/IN_PROGRESS/FINISHED/COMPLETED';
COMMENT ON COLUMN t_prod_order.remark IS '备注';
COMMENT ON COLUMN t_prod_order.create_time IS '创建时间';
COMMENT ON COLUMN t_prod_order.update_time IS '更新时间';
COMMENT ON COLUMN t_prod_order.create_by IS '创建人';
COMMENT ON COLUMN t_prod_order.update_by IS '更新人';
COMMENT ON COLUMN t_prod_order.deleted IS '逻辑删除';

CREATE INDEX idx_sales_order ON t_prod_order(sales_order_id);
CREATE INDEX idx_status ON t_prod_order(status);

CREATE TABLE t_prod_order_bom (
    id              BIGINT         NOT NULL PRIMARY KEY,
    order_id        BIGINT         NOT NULL,
    material_id     BIGINT         NOT NULL,
    required_qty    DECIMAL(18,4)  NOT NULL,
    picked_qty      DECIMAL(18,4)  DEFAULT 0,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_prod_order_bom IS '工单物料需求';
COMMENT ON COLUMN t_prod_order_bom.id IS '主键';
COMMENT ON COLUMN t_prod_order_bom.order_id IS '工单ID';
COMMENT ON COLUMN t_prod_order_bom.material_id IS '子物料ID';
COMMENT ON COLUMN t_prod_order_bom.required_qty IS '需求数量';
COMMENT ON COLUMN t_prod_order_bom.picked_qty IS '已领数量';
COMMENT ON COLUMN t_prod_order_bom.create_time IS '创建时间';
COMMENT ON COLUMN t_prod_order_bom.update_time IS '更新时间';

CREATE INDEX idx_order_id ON t_prod_order_bom(order_id);

CREATE TABLE t_prod_picking (
    id            BIGINT         NOT NULL PRIMARY KEY,
    picking_no    VARCHAR(30)    NOT NULL,
    order_id      BIGINT         NOT NULL,
    warehouse_id  BIGINT         NOT NULL,
    picking_date  DATE           DEFAULT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    remark        VARCHAR(500)   DEFAULT NULL,
    create_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by     BIGINT         DEFAULT NULL,
    update_by     BIGINT         DEFAULT NULL,
    deleted       SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_picking_no UNIQUE (picking_no)
);

COMMENT ON TABLE t_prod_picking IS '领料单主表';
COMMENT ON COLUMN t_prod_picking.id IS '领料单ID';
COMMENT ON COLUMN t_prod_picking.picking_no IS '领料单号';
COMMENT ON COLUMN t_prod_picking.order_id IS '工单ID';
COMMENT ON COLUMN t_prod_picking.warehouse_id IS '仓库ID';
COMMENT ON COLUMN t_prod_picking.picking_date IS '领料日期';
COMMENT ON COLUMN t_prod_picking.status IS '状态：DRAFT/CONFIRMED';
COMMENT ON COLUMN t_prod_picking.remark IS '备注';
COMMENT ON COLUMN t_prod_picking.create_time IS '创建时间';
COMMENT ON COLUMN t_prod_picking.update_time IS '更新时间';
COMMENT ON COLUMN t_prod_picking.create_by IS '创建人';
COMMENT ON COLUMN t_prod_picking.update_by IS '更新人';
COMMENT ON COLUMN t_prod_picking.deleted IS '逻辑删除';

CREATE INDEX idx_order_id ON t_prod_picking(order_id);

CREATE TABLE t_prod_picking_item (
    id            BIGINT         NOT NULL PRIMARY KEY,
    picking_id    BIGINT         NOT NULL,
    material_id   BIGINT         NOT NULL,
    request_qty   DECIMAL(18,2)  NOT NULL,
    actual_qty    DECIMAL(18,2)  NOT NULL,
    create_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_prod_picking_item IS '领料单明细表';
COMMENT ON COLUMN t_prod_picking_item.id IS '明细ID';
COMMENT ON COLUMN t_prod_picking_item.picking_id IS '领料单ID';
COMMENT ON COLUMN t_prod_picking_item.material_id IS '物料ID';
COMMENT ON COLUMN t_prod_picking_item.request_qty IS '申请数量';
COMMENT ON COLUMN t_prod_picking_item.actual_qty IS '实发数量';
COMMENT ON COLUMN t_prod_picking_item.create_time IS '创建时间';
COMMENT ON COLUMN t_prod_picking_item.update_time IS '更新时间';

CREATE INDEX idx_picking_id ON t_prod_picking_item(picking_id);

CREATE TABLE t_prod_finish (
    id            BIGINT         NOT NULL PRIMARY KEY,
    finish_no     VARCHAR(30)    NOT NULL,
    order_id      BIGINT         NOT NULL,
    warehouse_id  BIGINT         NOT NULL,
    finish_date   DATE           DEFAULT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    remark        VARCHAR(500)   DEFAULT NULL,
    create_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by     BIGINT         DEFAULT NULL,
    update_by     BIGINT         DEFAULT NULL,
    deleted       SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_finish_no UNIQUE (finish_no)
);

COMMENT ON TABLE t_prod_finish IS '完工入库单主表';
COMMENT ON COLUMN t_prod_finish.id IS '完工入库单ID';
COMMENT ON COLUMN t_prod_finish.finish_no IS '入库单号';
COMMENT ON COLUMN t_prod_finish.order_id IS '工单ID';
COMMENT ON COLUMN t_prod_finish.warehouse_id IS '仓库ID';
COMMENT ON COLUMN t_prod_finish.finish_date IS '入库日期';
COMMENT ON COLUMN t_prod_finish.status IS '状态：DRAFT/CONFIRMED';
COMMENT ON COLUMN t_prod_finish.remark IS '备注';
COMMENT ON COLUMN t_prod_finish.create_time IS '创建时间';
COMMENT ON COLUMN t_prod_finish.update_time IS '更新时间';
COMMENT ON COLUMN t_prod_finish.create_by IS '创建人';
COMMENT ON COLUMN t_prod_finish.update_by IS '更新人';
COMMENT ON COLUMN t_prod_finish.deleted IS '逻辑删除';

CREATE INDEX idx_order_id ON t_prod_finish(order_id);

CREATE TABLE t_prod_finish_item (
    id          BIGINT         NOT NULL PRIMARY KEY,
    finish_id   BIGINT         NOT NULL,
    material_id BIGINT         NOT NULL,
    quantity    DECIMAL(18,2)  NOT NULL,
    create_time TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_prod_finish_item IS '完工入库明细表';
COMMENT ON COLUMN t_prod_finish_item.id IS '明细ID';
COMMENT ON COLUMN t_prod_finish_item.finish_id IS '入库单ID';
COMMENT ON COLUMN t_prod_finish_item.material_id IS '物料ID';
COMMENT ON COLUMN t_prod_finish_item.quantity IS '入库数量';
COMMENT ON COLUMN t_prod_finish_item.create_time IS '创建时间';
COMMENT ON COLUMN t_prod_finish_item.update_time IS '更新时间';

CREATE INDEX idx_finish_id ON t_prod_finish_item(finish_id);

-- ==================== 采购管理 ====================

CREATE TABLE t_pur_requisition (
    id              BIGINT         NOT NULL PRIMARY KEY,
    requisition_no  VARCHAR(30)    NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    remark          VARCHAR(500)   DEFAULT NULL,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT         DEFAULT NULL,
    update_by       BIGINT         DEFAULT NULL,
    deleted         SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_requisition_no UNIQUE (requisition_no)
);

COMMENT ON TABLE t_pur_requisition IS '采购申请主表';
COMMENT ON COLUMN t_pur_requisition.id IS '采购申请ID';
COMMENT ON COLUMN t_pur_requisition.requisition_no IS '申请单号';
COMMENT ON COLUMN t_pur_requisition.status IS '状态：DRAFT/APPROVED/ORDERED';
COMMENT ON COLUMN t_pur_requisition.remark IS '备注';
COMMENT ON COLUMN t_pur_requisition.create_time IS '创建时间';
COMMENT ON COLUMN t_pur_requisition.update_time IS '更新时间';
COMMENT ON COLUMN t_pur_requisition.create_by IS '创建人';
COMMENT ON COLUMN t_pur_requisition.update_by IS '更新人';
COMMENT ON COLUMN t_pur_requisition.deleted IS '逻辑删除';

CREATE TABLE t_pur_requisition_item (
    id              BIGINT         NOT NULL PRIMARY KEY,
    requisition_id  BIGINT         NOT NULL,
    material_id     BIGINT         NOT NULL,
    quantity        DECIMAL(18,2)  NOT NULL,
    ordered_qty     DECIMAL(18,2)  DEFAULT 0,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_pur_requisition_item IS '采购申请明细表';
COMMENT ON COLUMN t_pur_requisition_item.id IS '明细ID';
COMMENT ON COLUMN t_pur_requisition_item.requisition_id IS '申请单ID';
COMMENT ON COLUMN t_pur_requisition_item.material_id IS '物料ID';
COMMENT ON COLUMN t_pur_requisition_item.quantity IS '申请数量';
COMMENT ON COLUMN t_pur_requisition_item.ordered_qty IS '已下单数量';
COMMENT ON COLUMN t_pur_requisition_item.create_time IS '创建时间';
COMMENT ON COLUMN t_pur_requisition_item.update_time IS '更新时间';

CREATE INDEX idx_requisition_id ON t_pur_requisition_item(requisition_id);

CREATE TABLE t_pur_order (
    id              BIGINT         NOT NULL PRIMARY KEY,
    order_no        VARCHAR(30)    NOT NULL,
    supplier_id     BIGINT         NOT NULL,
    order_date      DATE           NOT NULL,
    total_amount    DECIMAL(18,2)  DEFAULT 0,
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    remark          VARCHAR(500)   DEFAULT NULL,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT         DEFAULT NULL,
    update_by       BIGINT         DEFAULT NULL,
    deleted         SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_order_no UNIQUE (order_no)
);

COMMENT ON TABLE t_pur_order IS '采购订单主表';
COMMENT ON COLUMN t_pur_order.id IS '采购订单ID';
COMMENT ON COLUMN t_pur_order.order_no IS '订单号 (PO + yyyyMMdd + 流水)';
COMMENT ON COLUMN t_pur_order.supplier_id IS '供应商ID';
COMMENT ON COLUMN t_pur_order.order_date IS '订单日期';
COMMENT ON COLUMN t_pur_order.total_amount IS '总金额';
COMMENT ON COLUMN t_pur_order.status IS '状态：DRAFT/SUBMITTED/APPROVED/PARTIAL_RECEIVED/COMPLETED/CLOSED';
COMMENT ON COLUMN t_pur_order.remark IS '备注';
COMMENT ON COLUMN t_pur_order.create_time IS '创建时间';
COMMENT ON COLUMN t_pur_order.update_time IS '更新时间';
COMMENT ON COLUMN t_pur_order.create_by IS '创建人';
COMMENT ON COLUMN t_pur_order.update_by IS '更新人';
COMMENT ON COLUMN t_pur_order.deleted IS '逻辑删除';

CREATE INDEX idx_supplier ON t_pur_order(supplier_id);
CREATE INDEX idx_status ON t_pur_order(status);

CREATE TABLE t_pur_order_item (
    id            BIGINT         NOT NULL PRIMARY KEY,
    order_id      BIGINT         NOT NULL,
    line_no       INT            NOT NULL,
    material_id   BIGINT         NOT NULL,
    quantity      DECIMAL(18,2)  NOT NULL,
    unit          VARCHAR(20)    NOT NULL,
    price         DECIMAL(18,2)  NOT NULL,
    amount        DECIMAL(18,2)  NOT NULL,
    received_qty  DECIMAL(18,2)  DEFAULT 0,
    create_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_pur_order_item IS '采购订单明细表';
COMMENT ON COLUMN t_pur_order_item.id IS '明细ID';
COMMENT ON COLUMN t_pur_order_item.order_id IS '订单ID';
COMMENT ON COLUMN t_pur_order_item.line_no IS '行号';
COMMENT ON COLUMN t_pur_order_item.material_id IS '物料ID';
COMMENT ON COLUMN t_pur_order_item.quantity IS '数量';
COMMENT ON COLUMN t_pur_order_item.unit IS '单位';
COMMENT ON COLUMN t_pur_order_item.price IS '单价';
COMMENT ON COLUMN t_pur_order_item.amount IS '金额';
COMMENT ON COLUMN t_pur_order_item.received_qty IS '已收货数量';
COMMENT ON COLUMN t_pur_order_item.create_time IS '创建时间';
COMMENT ON COLUMN t_pur_order_item.update_time IS '更新时间';

CREATE INDEX idx_order_id ON t_pur_order_item(order_id);

CREATE TABLE t_pur_receiving (
    id              BIGINT         NOT NULL PRIMARY KEY,
    receiving_no    VARCHAR(30)    NOT NULL,
    order_id        BIGINT         NOT NULL,
    warehouse_id    BIGINT         NOT NULL,
    receiving_date  DATE           DEFAULT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    remark          VARCHAR(500)   DEFAULT NULL,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT         DEFAULT NULL,
    update_by       BIGINT         DEFAULT NULL,
    deleted         SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_receiving_no UNIQUE (receiving_no)
);

COMMENT ON TABLE t_pur_receiving IS '收货单主表';
COMMENT ON COLUMN t_pur_receiving.id IS '收货单ID';
COMMENT ON COLUMN t_pur_receiving.receiving_no IS '收货单号';
COMMENT ON COLUMN t_pur_receiving.order_id IS '采购订单ID';
COMMENT ON COLUMN t_pur_receiving.warehouse_id IS '仓库ID';
COMMENT ON COLUMN t_pur_receiving.receiving_date IS '收货日期';
COMMENT ON COLUMN t_pur_receiving.status IS '状态：DRAFT/CONFIRMED';
COMMENT ON COLUMN t_pur_receiving.remark IS '备注';
COMMENT ON COLUMN t_pur_receiving.create_time IS '创建时间';
COMMENT ON COLUMN t_pur_receiving.update_time IS '更新时间';
COMMENT ON COLUMN t_pur_receiving.create_by IS '创建人';
COMMENT ON COLUMN t_pur_receiving.update_by IS '更新人';
COMMENT ON COLUMN t_pur_receiving.deleted IS '逻辑删除';

CREATE INDEX idx_order_id ON t_pur_receiving(order_id);

CREATE TABLE t_pur_receiving_item (
    id              BIGINT         NOT NULL PRIMARY KEY,
    receiving_id    BIGINT         NOT NULL,
    order_item_id   BIGINT         NOT NULL,
    material_id     BIGINT         NOT NULL,
    quantity        DECIMAL(18,2)  NOT NULL,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_pur_receiving_item IS '收货单明细表';
COMMENT ON COLUMN t_pur_receiving_item.id IS '明细ID';
COMMENT ON COLUMN t_pur_receiving_item.receiving_id IS '收货单ID';
COMMENT ON COLUMN t_pur_receiving_item.order_item_id IS '订单明细ID';
COMMENT ON COLUMN t_pur_receiving_item.material_id IS '物料ID';
COMMENT ON COLUMN t_pur_receiving_item.quantity IS '收货数量';
COMMENT ON COLUMN t_pur_receiving_item.create_time IS '创建时间';
COMMENT ON COLUMN t_pur_receiving_item.update_time IS '更新时间';

CREATE INDEX idx_receiving_id ON t_pur_receiving_item(receiving_id);

-- ==================== 库存管理 ====================

CREATE TABLE t_inv_stock (
    id              BIGINT         NOT NULL PRIMARY KEY,
    material_id     BIGINT         NOT NULL,
    warehouse_id    BIGINT         NOT NULL,
    quantity        DECIMAL(18,4)  NOT NULL DEFAULT 0,
    available_qty   DECIMAL(18,4)  NOT NULL DEFAULT 0,
    locked_qty      DECIMAL(18,4)  NOT NULL DEFAULT 0,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_material_warehouse UNIQUE (material_id, warehouse_id)
);

COMMENT ON TABLE t_inv_stock IS '库存表';
COMMENT ON COLUMN t_inv_stock.id IS '主键';
COMMENT ON COLUMN t_inv_stock.material_id IS '物料ID';
COMMENT ON COLUMN t_inv_stock.warehouse_id IS '仓库ID';
COMMENT ON COLUMN t_inv_stock.quantity IS '库存数量';
COMMENT ON COLUMN t_inv_stock.available_qty IS '可用数量';
COMMENT ON COLUMN t_inv_stock.locked_qty IS '锁定数量';
COMMENT ON COLUMN t_inv_stock.create_time IS '创建时间';
COMMENT ON COLUMN t_inv_stock.update_time IS '更新时间';

CREATE INDEX idx_material ON t_inv_stock(material_id);
CREATE INDEX idx_warehouse ON t_inv_stock(warehouse_id);

CREATE TABLE t_inv_transaction (
    id              BIGINT         NOT NULL PRIMARY KEY,
    material_id     BIGINT         NOT NULL,
    warehouse_id    BIGINT         NOT NULL,
    type            VARCHAR(20)    NOT NULL,
    quantity        DECIMAL(18,4)  NOT NULL,
    current_stock   DECIMAL(18,4)  NOT NULL,
    source_no       VARCHAR(30)    DEFAULT NULL,
    source_type     VARCHAR(30)    DEFAULT NULL,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT         DEFAULT NULL
);

COMMENT ON TABLE t_inv_transaction IS '库存流水表';
COMMENT ON COLUMN t_inv_transaction.id IS '流水ID';
COMMENT ON COLUMN t_inv_transaction.material_id IS '物料ID';
COMMENT ON COLUMN t_inv_transaction.warehouse_id IS '仓库ID';
COMMENT ON COLUMN t_inv_transaction.type IS '变动类型：PURCHASE_IN/PICKING_OUT/PICKING_RETURN/FINISH_IN/SHIPPING_OUT/TRANSFER/ADJUST/SCRAP';
COMMENT ON COLUMN t_inv_transaction.quantity IS '变动数量（正=入库，负=出库）';
COMMENT ON COLUMN t_inv_transaction.current_stock IS '变动后库存';
COMMENT ON COLUMN t_inv_transaction.source_no IS '来源单号';
COMMENT ON COLUMN t_inv_transaction.source_type IS '来源单据类型';
COMMENT ON COLUMN t_inv_transaction.create_time IS '创建时间';
COMMENT ON COLUMN t_inv_transaction.create_by IS '操作人';

CREATE INDEX idx_material_warehouse ON t_inv_transaction(material_id, warehouse_id);
CREATE INDEX idx_create_time ON t_inv_transaction(create_time);
CREATE INDEX idx_source ON t_inv_transaction(source_type, source_no);

-- ==================== 财务管理 ====================

CREATE TABLE t_fin_receivable (
    id                BIGINT         NOT NULL PRIMARY KEY,
    delivery_id       BIGINT         NOT NULL,
    customer_id       BIGINT         NOT NULL,
    receivable_amount DECIMAL(18,2)  NOT NULL,
    received_amount   DECIMAL(18,2)  DEFAULT 0,
    status            VARCHAR(20)    NOT NULL DEFAULT 'UNPAID',
    create_time       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by         BIGINT         DEFAULT NULL,
    update_by         BIGINT         DEFAULT NULL,
    deleted           SMALLINT       NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_fin_receivable IS '应收台账';
COMMENT ON COLUMN t_fin_receivable.id IS '应收ID';
COMMENT ON COLUMN t_fin_receivable.delivery_id IS '发货单ID';
COMMENT ON COLUMN t_fin_receivable.customer_id IS '客户ID';
COMMENT ON COLUMN t_fin_receivable.receivable_amount IS '应收金额';
COMMENT ON COLUMN t_fin_receivable.received_amount IS '已收金额';
COMMENT ON COLUMN t_fin_receivable.status IS '状态：UNPAID/PARTIAL_PAID/PAID';
COMMENT ON COLUMN t_fin_receivable.create_time IS '创建时间';
COMMENT ON COLUMN t_fin_receivable.update_time IS '更新时间';
COMMENT ON COLUMN t_fin_receivable.create_by IS '创建人';
COMMENT ON COLUMN t_fin_receivable.update_by IS '更新人';
COMMENT ON COLUMN t_fin_receivable.deleted IS '逻辑删除';

CREATE INDEX idx_customer ON t_fin_receivable(customer_id);
CREATE INDEX idx_status ON t_fin_receivable(status);

CREATE TABLE t_fin_payable (
    id              BIGINT         NOT NULL PRIMARY KEY,
    receiving_id    BIGINT         NOT NULL,
    supplier_id     BIGINT         NOT NULL,
    payable_amount  DECIMAL(18,2)  NOT NULL,
    paid_amount     DECIMAL(18,2)  DEFAULT 0,
    status          VARCHAR(20)    NOT NULL DEFAULT 'UNPAID',
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT         DEFAULT NULL,
    update_by       BIGINT         DEFAULT NULL,
    deleted         SMALLINT       NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_fin_payable IS '应付台账';
COMMENT ON COLUMN t_fin_payable.id IS '应付ID';
COMMENT ON COLUMN t_fin_payable.receiving_id IS '收货单ID';
COMMENT ON COLUMN t_fin_payable.supplier_id IS '供应商ID';
COMMENT ON COLUMN t_fin_payable.payable_amount IS '应付金额';
COMMENT ON COLUMN t_fin_payable.paid_amount IS '已付金额';
COMMENT ON COLUMN t_fin_payable.status IS '状态：UNPAID/PARTIAL_PAID/PAID';
COMMENT ON COLUMN t_fin_payable.create_time IS '创建时间';
COMMENT ON COLUMN t_fin_payable.update_time IS '更新时间';
COMMENT ON COLUMN t_fin_payable.create_by IS '创建人';
COMMENT ON COLUMN t_fin_payable.update_by IS '更新人';
COMMENT ON COLUMN t_fin_payable.deleted IS '逻辑删除';

CREATE INDEX idx_supplier ON t_fin_payable(supplier_id);
CREATE INDEX idx_status ON t_fin_payable(status);

CREATE TABLE t_fin_payment (
    id              BIGINT         NOT NULL PRIMARY KEY,
    payment_no      VARCHAR(30)    NOT NULL,
    type            VARCHAR(10)    NOT NULL,
    counterparty_id BIGINT         NOT NULL,
    amount          DECIMAL(18,2)  NOT NULL,
    bank_account    VARCHAR(50)    DEFAULT NULL,
    payment_date    DATE           NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    remark          VARCHAR(500)   DEFAULT NULL,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT         DEFAULT NULL,
    update_by       BIGINT         DEFAULT NULL,
    deleted         SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_payment_no UNIQUE (payment_no)
);

COMMENT ON TABLE t_fin_payment IS '收付款单';
COMMENT ON COLUMN t_fin_payment.id IS '收付款单ID';
COMMENT ON COLUMN t_fin_payment.payment_no IS '收付款单号';
COMMENT ON COLUMN t_fin_payment.type IS '类型：RECEIVE/PAY';
COMMENT ON COLUMN t_fin_payment.counterparty_id IS '对方ID（客户/供应商）';
COMMENT ON COLUMN t_fin_payment.amount IS '金额';
COMMENT ON COLUMN t_fin_payment.bank_account IS '银行账号';
COMMENT ON COLUMN t_fin_payment.payment_date IS '收付款日期';
COMMENT ON COLUMN t_fin_payment.status IS '状态：DRAFT/CONFIRMED';
COMMENT ON COLUMN t_fin_payment.remark IS '备注';
COMMENT ON COLUMN t_fin_payment.create_time IS '创建时间';
COMMENT ON COLUMN t_fin_payment.update_time IS '更新时间';
COMMENT ON COLUMN t_fin_payment.create_by IS '创建人';
COMMENT ON COLUMN t_fin_payment.update_by IS '更新人';
COMMENT ON COLUMN t_fin_payment.deleted IS '逻辑删除';

CREATE INDEX idx_counterparty ON t_fin_payment(counterparty_id);

CREATE TABLE t_fin_payment_item (
    id              BIGINT         NOT NULL PRIMARY KEY,
    payment_id      BIGINT         NOT NULL,
    receivable_id   BIGINT         DEFAULT NULL,
    payable_id      BIGINT         DEFAULT NULL,
    amount          DECIMAL(18,2)  NOT NULL,
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_fin_payment_item IS '收付款核销明细表';
COMMENT ON COLUMN t_fin_payment_item.id IS '核销明细ID';
COMMENT ON COLUMN t_fin_payment_item.payment_id IS '收付款单ID';
COMMENT ON COLUMN t_fin_payment_item.receivable_id IS '应收ID（收款时）';
COMMENT ON COLUMN t_fin_payment_item.payable_id IS '应付ID（付款时）';
COMMENT ON COLUMN t_fin_payment_item.amount IS '核销金额';
COMMENT ON COLUMN t_fin_payment_item.create_time IS '创建时间';

CREATE INDEX idx_payment_id ON t_fin_payment_item(payment_id);

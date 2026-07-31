-- =====================================================
-- V1__init_schema.sql — 全部表结构（合并自 V1~V22）
-- =====================================================

-- ==================== 系统管理 ====================

CREATE TABLE t_sys_user (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '账号',
    password    VARCHAR(200) NOT NULL COMMENT '密码（加密）',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    employee_id BIGINT       DEFAULT NULL COMMENT '关联员工ID',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE t_sys_role (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '角色ID',
    name        VARCHAR(50)  NOT NULL COMMENT '角色名称',
    code        VARCHAR(50)  NOT NULL COMMENT '角色编码',
    remark      VARCHAR(200) DEFAULT NULL COMMENT '备注',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE t_sys_permission (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '权限ID',
    parent_id   BIGINT       DEFAULT 0 COMMENT '父权限ID',
    name        VARCHAR(50)  NOT NULL COMMENT '权限名称',
    code        VARCHAR(100) DEFAULT NULL COMMENT '权限编码',
    type        TINYINT      NOT NULL DEFAULT 1 COMMENT '类型：1-菜单 2-按钮 3-接口',
    path        VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    icon        VARCHAR(50)  DEFAULT NULL COMMENT '图标',
    sort        INT          DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE t_sys_user_role (
    id      BIGINT NOT NULL PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE t_sys_role_permission (
    id            BIGINT NOT NULL PRIMARY KEY COMMENT '主键',
    role_id       BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE t_sys_dict (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '字典ID',
    name        VARCHAR(50)  NOT NULL COMMENT '字典名称',
    code        VARCHAR(50)  NOT NULL COMMENT '字典编码',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典类型表';

CREATE TABLE t_sys_dict_item (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '明细ID',
    dict_id     BIGINT       NOT NULL COMMENT '字典ID',
    label       VARCHAR(100) NOT NULL COMMENT '标签',
    value       VARCHAR(100) NOT NULL COMMENT '值',
    sort        INT          DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_dict_id (dict_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典明细表';

-- ==================== 基础数据 ====================

CREATE TABLE t_base_material (
    id              BIGINT        NOT NULL PRIMARY KEY COMMENT '物料ID',
    code            VARCHAR(50)   NOT NULL COMMENT '物料编码',
    name            VARCHAR(100)  NOT NULL COMMENT '物料名称',
    spec            VARCHAR(100)  DEFAULT NULL COMMENT '规格',
    unit            VARCHAR(20)   NOT NULL COMMENT '单位',
    category        TINYINT       NOT NULL COMMENT '类型：1-原材料 2-半成品 3-成品 4-辅料',
    safety_stock    DECIMAL(18,2) DEFAULT 0 COMMENT '安全库存',
    price           DECIMAL(18,2) DEFAULT 0 COMMENT '参考单价',
    status          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    remark          VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT        DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT        DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料表';

CREATE TABLE t_bom_header (
    id                 BIGINT        NOT NULL PRIMARY KEY COMMENT '主键',
    bom_no             VARCHAR(30)   NOT NULL COMMENT 'BOM编号',
    product_material_id BIGINT       NOT NULL COMMENT '成品物料ID',
    version            VARCHAR(20)   DEFAULT '1.0' COMMENT '版本号',
    status             TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    create_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by          BIGINT        DEFAULT NULL COMMENT '创建人',
    update_by          BIGINT        DEFAULT NULL COMMENT '更新人',
    deleted            TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_bom_no (bom_no),
    INDEX idx_product (product_material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BOM头表';

CREATE TABLE t_bom_detail (
    id          BIGINT        NOT NULL PRIMARY KEY COMMENT '主键',
    bom_id      BIGINT        NOT NULL COMMENT '关联 t_bom_header.id',
    material_id BIGINT        NOT NULL COMMENT '原材料ID',
    quantity    DECIMAL(18,4) NOT NULL COMMENT '用量',
    unit        VARCHAR(20)   DEFAULT NULL COMMENT '单位',
    remark      VARCHAR(200)  DEFAULT NULL COMMENT '备注',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_bom_id (bom_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BOM明细表';

CREATE TABLE t_base_customer (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '客户ID',
    code        VARCHAR(50)  NOT NULL COMMENT '客户编码',
    name        VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact     VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    phone       VARCHAR(30)  DEFAULT NULL COMMENT '联系电话',
    address     VARCHAR(300) DEFAULT NULL COMMENT '地址',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

CREATE TABLE t_base_supplier (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '供应商ID',
    code        VARCHAR(50)  NOT NULL COMMENT '供应商编码',
    name        VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact     VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    phone       VARCHAR(30)  DEFAULT NULL COMMENT '联系电话',
    address     VARCHAR(300) DEFAULT NULL COMMENT '地址',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

CREATE TABLE t_base_warehouse (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '仓库ID',
    code        VARCHAR(50)  NOT NULL COMMENT '仓库编码',
    name        VARCHAR(100) NOT NULL COMMENT '仓库名称',
    address     VARCHAR(300) DEFAULT NULL COMMENT '仓库地址',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

CREATE TABLE t_base_employee (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '员工ID',
    code        VARCHAR(50)  NOT NULL COMMENT '员工编码',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    gender      TINYINT      DEFAULT NULL COMMENT '性别：1-男 2-女',
    phone       VARCHAR(30)  DEFAULT NULL COMMENT '手机号',
    dept        VARCHAR(50)  DEFAULT NULL COMMENT '部门',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 公司账户（合并 V9 + V15 create_by/update_by）
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
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by     BIGINT        DEFAULT NULL COMMENT '创建人',
    update_by     BIGINT        DEFAULT NULL COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司账户表';

-- 客户/供应商银行账户（合并 V13 + V15 create_by/update_by）
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
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by     BIGINT        DEFAULT NULL COMMENT '创建人',
    update_by     BIGINT        DEFAULT NULL COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='往来方账户表';

-- ==================== 销售管理 ====================

CREATE TABLE t_sales_order (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '订单ID',
    order_no        VARCHAR(30)    NOT NULL COMMENT '订单号 (SO + yyyyMMdd + 4位流水)',
    customer_id     BIGINT         NOT NULL COMMENT '客户ID',
    order_date      DATE           NOT NULL COMMENT '订单日期',
    delivery_date   DATE           DEFAULT NULL COMMENT '交货日期',
    customer_po_no  VARCHAR(100)   DEFAULT NULL COMMENT '客户采购订单号',
    total_amount    DECIMAL(18,2)  DEFAULT 0 COMMENT '总金额',
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/SUBMITTED/APPROVED/PARTIAL_SHIPPED/COMPLETED/CLOSED',
    remark          VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单主表';

CREATE TABLE t_sales_order_item (
    id          BIGINT         NOT NULL PRIMARY KEY COMMENT '明细ID',
    order_id    BIGINT         NOT NULL COMMENT '订单ID',
    line_no     INT            NOT NULL COMMENT '行号',
    material_id BIGINT         NOT NULL COMMENT '物料ID',
    quantity    DECIMAL(18,2)  NOT NULL COMMENT '数量',
    unit        VARCHAR(20)    NOT NULL COMMENT '单位',
    price       DECIMAL(18,2)  NOT NULL COMMENT '单价',
    amount      DECIMAL(18,2)  NOT NULL COMMENT '金额',
    shipped_qty DECIMAL(18,2)  DEFAULT 0 COMMENT '已发货数量',
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单明细表';

CREATE TABLE t_sales_delivery (
    id            BIGINT         NOT NULL PRIMARY KEY COMMENT '发货单ID',
    delivery_no   VARCHAR(30)    NOT NULL COMMENT '发货单号',
    order_id      BIGINT         NOT NULL COMMENT '销售订单ID',
    warehouse_id  BIGINT         NOT NULL COMMENT '仓库ID',
    delivery_date DATE           DEFAULT NULL COMMENT '发货日期',
    status        VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/CONFIRMED',
    remark        VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by     BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by     BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted       TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_delivery_no (delivery_no),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发货单主表';

CREATE TABLE t_sales_delivery_item (
    id            BIGINT         NOT NULL PRIMARY KEY COMMENT '明细ID',
    delivery_id   BIGINT         NOT NULL COMMENT '发货单ID',
    order_item_id BIGINT         NOT NULL COMMENT '订单明细ID',
    material_id   BIGINT         NOT NULL COMMENT '物料ID',
    quantity      DECIMAL(18,2)  NOT NULL COMMENT '实际发货数量',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_delivery_id (delivery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发货单明细表';

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
    applicant_id    BIGINT         DEFAULT NULL COMMENT '申请人（员工ID）',
    req_date        DATE           DEFAULT NULL COMMENT '申请日期',
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
    requisition_id  BIGINT         DEFAULT NULL COMMENT '采购申请ID',
    order_date      DATE           NOT NULL COMMENT '订单日期',
    delivery_date   DATE           DEFAULT NULL COMMENT '交货日期',
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

-- 库存调拨单（V19）
CREATE TABLE t_inv_transfer (
    id                BIGINT        NOT NULL PRIMARY KEY COMMENT '调拨单ID',
    transfer_no       VARCHAR(50)   NOT NULL COMMENT '调拨单号',
    from_warehouse_id BIGINT        NOT NULL COMMENT '调出仓库ID',
    to_warehouse_id   BIGINT        NOT NULL COMMENT '调入仓库ID',
    transfer_date     DATE          DEFAULT NULL COMMENT '调拨日期',
    status            VARCHAR(20)   DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED',
    remark            VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by         BIGINT        DEFAULT NULL COMMENT '创建人',
    update_by         BIGINT        DEFAULT NULL COMMENT '更新人',
    deleted           TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调拨单';

CREATE TABLE t_inv_transfer_item (
    id            BIGINT        NOT NULL PRIMARY KEY COMMENT '调拨明细ID',
    transfer_id   BIGINT        NOT NULL COMMENT '调拨单ID',
    material_id   BIGINT        NOT NULL COMMENT '物料ID',
    quantity      DECIMAL(18,4) NOT NULL COMMENT '调拨数量',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by     BIGINT        DEFAULT NULL COMMENT '创建人',
    update_by     BIGINT        DEFAULT NULL COMMENT '更新人',
    INDEX idx_transfer_id (transfer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调拨明细表';

-- ==================== 财务管理 ====================

CREATE TABLE t_fin_receivable (
    id                BIGINT         NOT NULL PRIMARY KEY COMMENT '应收ID',
    delivery_id       BIGINT         NOT NULL COMMENT '发货单ID',
    customer_id       BIGINT         NOT NULL COMMENT '客户ID',
    receivable_amount DECIMAL(18,2)  NOT NULL COMMENT '应收金额',
    received_amount   DECIMAL(18,2)  DEFAULT 0 COMMENT '已收金额',
    due_date          DATE           DEFAULT NULL COMMENT '应收日期',
    status            VARCHAR(20)    NOT NULL DEFAULT 'UNPAID' COMMENT '状态：UNPAID/PARTIAL_PAID/PAID',
    create_time       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by         BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by         BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted           TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_customer (customer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应收台账';

CREATE TABLE t_fin_payable (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '应付ID',
    receiving_id    BIGINT         NOT NULL COMMENT '收货单ID',
    supplier_id     BIGINT         NOT NULL COMMENT '供应商ID',
    payable_amount  DECIMAL(18,2)  NOT NULL COMMENT '应付金额',
    paid_amount     DECIMAL(18,2)  DEFAULT 0 COMMENT '已付金额',
    due_date        DATE           DEFAULT NULL COMMENT '应付日期',
    status          VARCHAR(20)    NOT NULL DEFAULT 'UNPAID' COMMENT '状态：UNPAID/PARTIAL_PAID/PAID',
    create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_supplier (supplier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应付台账';

-- 收付款单（bank_account 已替换为 company_account_id，合并 V8 reconciled_amount）
CREATE TABLE t_fin_payment (
    id                  BIGINT         NOT NULL PRIMARY KEY COMMENT '收付款单ID',
    payment_no          VARCHAR(30)    NOT NULL COMMENT '收付款单号',
    type                VARCHAR(10)    NOT NULL COMMENT '类型：RECEIVE/PAY',
    counterparty_id     BIGINT         NOT NULL COMMENT '对方ID（客户/供应商）',
    amount              DECIMAL(18,2)  NOT NULL COMMENT '金额',
    company_account_id  BIGINT         DEFAULT NULL COMMENT '公司账户ID',
    reconciled_amount   DECIMAL(18,2)  DEFAULT 0 COMMENT '已核销金额',
    payment_date        DATE           NOT NULL COMMENT '收付款日期',
    status              VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/CONFIRMED',
    remark              VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    create_time         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT         DEFAULT NULL COMMENT '创建人',
    update_by           BIGINT         DEFAULT NULL COMMENT '更新人',
    deleted             TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
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

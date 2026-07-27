-- =====================================================
-- V1__init_schema.sql — 一期核心表结构
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

CREATE TABLE t_base_bom (
    id              BIGINT        NOT NULL PRIMARY KEY COMMENT 'BOM明细ID',
    parent_material_id BIGINT     NOT NULL COMMENT '父物料ID（成品/半成品）',
    child_material_id  BIGINT     NOT NULL COMMENT '子物料ID（原材料/半成品）',
    quantity        DECIMAL(18,4) NOT NULL COMMENT '用量',
    loss_rate       DECIMAL(5,2)  DEFAULT 0 COMMENT '损耗率(%)',
    sort            INT           DEFAULT 0 COMMENT '序号',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT        DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT        DEFAULT NULL COMMENT '更新人',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent (parent_material_id),
    INDEX idx_child (child_material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BOM表';

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

-- ==================== 销售管理 ====================

CREATE TABLE t_sales_order (
    id              BIGINT         NOT NULL PRIMARY KEY COMMENT '订单ID',
    order_no        VARCHAR(30)    NOT NULL COMMENT '订单号 (SO + yyyyMMdd + 4位流水)',
    customer_id     BIGINT         NOT NULL COMMENT '客户ID',
    order_date      DATE           NOT NULL COMMENT '订单日期',
    delivery_date   DATE           DEFAULT NULL COMMENT '交货日期',
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

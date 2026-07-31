-- =====================================================
-- V1__init_schema.sql (PostgreSQL) — 全部表结构（合并自 MySQL V1~V22）
-- =====================================================

-- ==================== 系统管理 ====================

CREATE TABLE t_sys_user (
    id          BIGINT       NOT NULL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(200) NOT NULL,
    nickname    VARCHAR(50)  DEFAULT NULL,
    employee_id BIGINT       DEFAULT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_username UNIQUE (username)
);

COMMENT ON TABLE t_sys_user IS '用户表';
COMMENT ON COLUMN t_sys_user.id IS '用户ID';
COMMENT ON COLUMN t_sys_user.username IS '账号';
COMMENT ON COLUMN t_sys_user.password IS '密码（加密）';
COMMENT ON COLUMN t_sys_user.nickname IS '昵称';
COMMENT ON COLUMN t_sys_user.employee_id IS '关联员工ID';
COMMENT ON COLUMN t_sys_user.status IS '状态：1-启用 0-禁用';
COMMENT ON COLUMN t_sys_user.create_time IS '创建时间';
COMMENT ON COLUMN t_sys_user.update_time IS '更新时间';
COMMENT ON COLUMN t_sys_user.create_by IS '创建人';
COMMENT ON COLUMN t_sys_user.update_by IS '更新人';
COMMENT ON COLUMN t_sys_user.deleted IS '逻辑删除：0-未删除 1-已删除';

CREATE TABLE t_sys_role (
    id          BIGINT       NOT NULL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    remark      VARCHAR(200) DEFAULT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_code UNIQUE (code)
);

COMMENT ON TABLE t_sys_role IS '角色表';
COMMENT ON COLUMN t_sys_role.id IS '角色ID';
COMMENT ON COLUMN t_sys_role.name IS '角色名称';
COMMENT ON COLUMN t_sys_role.code IS '角色编码';
COMMENT ON COLUMN t_sys_role.remark IS '备注';
COMMENT ON COLUMN t_sys_role.status IS '状态：1-启用 0-禁用';
COMMENT ON COLUMN t_sys_role.create_time IS '创建时间';
COMMENT ON COLUMN t_sys_role.update_time IS '更新时间';
COMMENT ON COLUMN t_sys_role.create_by IS '创建人';
COMMENT ON COLUMN t_sys_role.update_by IS '更新人';
COMMENT ON COLUMN t_sys_role.deleted IS '逻辑删除';

CREATE TABLE t_sys_permission (
    id          BIGINT       NOT NULL PRIMARY KEY,
    parent_id   BIGINT       DEFAULT 0,
    name        VARCHAR(50)  NOT NULL,
    code        VARCHAR(100) DEFAULT NULL,
    type        SMALLINT     NOT NULL DEFAULT 1,
    path        VARCHAR(200) DEFAULT NULL,
    icon        VARCHAR(50)  DEFAULT NULL,
    sort        INT          DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_sys_permission IS '权限表';
COMMENT ON COLUMN t_sys_permission.id IS '权限ID';
COMMENT ON COLUMN t_sys_permission.parent_id IS '父权限ID';
COMMENT ON COLUMN t_sys_permission.name IS '权限名称';
COMMENT ON COLUMN t_sys_permission.code IS '权限编码';
COMMENT ON COLUMN t_sys_permission.type IS '类型：1-菜单 2-按钮 3-接口';
COMMENT ON COLUMN t_sys_permission.path IS '路由路径';
COMMENT ON COLUMN t_sys_permission.icon IS '图标';
COMMENT ON COLUMN t_sys_permission.sort IS '排序';
COMMENT ON COLUMN t_sys_permission.status IS '状态';
COMMENT ON COLUMN t_sys_permission.create_time IS '创建时间';
COMMENT ON COLUMN t_sys_permission.update_time IS '更新时间';
COMMENT ON COLUMN t_sys_permission.create_by IS '创建人';
COMMENT ON COLUMN t_sys_permission.update_by IS '更新人';
COMMENT ON COLUMN t_sys_permission.deleted IS '逻辑删除';

CREATE TABLE t_sys_user_role (
    id      BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

COMMENT ON TABLE t_sys_user_role IS '用户角色关联表';
COMMENT ON COLUMN t_sys_user_role.id IS '主键';
COMMENT ON COLUMN t_sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN t_sys_user_role.role_id IS '角色ID';

CREATE INDEX idx_user_id ON t_sys_user_role(user_id);
CREATE INDEX idx_role_id ON t_sys_user_role(role_id);

CREATE TABLE t_sys_role_permission (
    id            BIGINT NOT NULL PRIMARY KEY,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT uk_role_perm UNIQUE (role_id, permission_id)
);

COMMENT ON TABLE t_sys_role_permission IS '角色权限关联表';
COMMENT ON COLUMN t_sys_role_permission.id IS '主键';
COMMENT ON COLUMN t_sys_role_permission.role_id IS '角色ID';
COMMENT ON COLUMN t_sys_role_permission.permission_id IS '权限ID';

CREATE INDEX idx_role_id ON t_sys_role_permission(role_id);
CREATE INDEX idx_permission_id ON t_sys_role_permission(permission_id);

CREATE TABLE t_sys_dict (
    id          BIGINT       NOT NULL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_code UNIQUE (code)
);

COMMENT ON TABLE t_sys_dict IS '数据字典类型表';
COMMENT ON COLUMN t_sys_dict.id IS '字典ID';
COMMENT ON COLUMN t_sys_dict.name IS '字典名称';
COMMENT ON COLUMN t_sys_dict.code IS '字典编码';
COMMENT ON COLUMN t_sys_dict.status IS '状态';
COMMENT ON COLUMN t_sys_dict.create_time IS '创建时间';
COMMENT ON COLUMN t_sys_dict.update_time IS '更新时间';
COMMENT ON COLUMN t_sys_dict.create_by IS '创建人';
COMMENT ON COLUMN t_sys_dict.update_by IS '更新人';
COMMENT ON COLUMN t_sys_dict.deleted IS '逻辑删除';

CREATE TABLE t_sys_dict_item (
    id          BIGINT       NOT NULL PRIMARY KEY,
    dict_id     BIGINT       NOT NULL,
    label       VARCHAR(100) NOT NULL,
    value       VARCHAR(100) NOT NULL,
    sort        INT          DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_sys_dict_item IS '数据字典明细表';
COMMENT ON COLUMN t_sys_dict_item.id IS '明细ID';
COMMENT ON COLUMN t_sys_dict_item.dict_id IS '字典ID';
COMMENT ON COLUMN t_sys_dict_item.label IS '标签';
COMMENT ON COLUMN t_sys_dict_item.value IS '值';
COMMENT ON COLUMN t_sys_dict_item.sort IS '排序';
COMMENT ON COLUMN t_sys_dict_item.status IS '状态';
COMMENT ON COLUMN t_sys_dict_item.create_time IS '创建时间';
COMMENT ON COLUMN t_sys_dict_item.update_time IS '更新时间';
COMMENT ON COLUMN t_sys_dict_item.create_by IS '创建人';
COMMENT ON COLUMN t_sys_dict_item.update_by IS '更新人';
COMMENT ON COLUMN t_sys_dict_item.deleted IS '逻辑删除';

CREATE INDEX idx_dict_id ON t_sys_dict_item(dict_id);

-- ==================== 基础数据 ====================

CREATE TABLE t_base_material (
    id              BIGINT        NOT NULL PRIMARY KEY,
    code            VARCHAR(50)   NOT NULL,
    name            VARCHAR(100)  NOT NULL,
    spec            VARCHAR(100)  DEFAULT NULL,
    unit            VARCHAR(20)   NOT NULL,
    category        SMALLINT      NOT NULL,
    safety_stock    DECIMAL(18,2) DEFAULT 0,
    price           DECIMAL(18,2) DEFAULT 0,
    status          SMALLINT      NOT NULL DEFAULT 1,
    remark          VARCHAR(500)  DEFAULT NULL,
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT        DEFAULT NULL,
    update_by       BIGINT        DEFAULT NULL,
    deleted         SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_code UNIQUE (code)
);

COMMENT ON TABLE t_base_material IS '物料表';
COMMENT ON COLUMN t_base_material.id IS '物料ID';
COMMENT ON COLUMN t_base_material.code IS '物料编码';
COMMENT ON COLUMN t_base_material.name IS '物料名称';
COMMENT ON COLUMN t_base_material.spec IS '规格';
COMMENT ON COLUMN t_base_material.unit IS '单位';
COMMENT ON COLUMN t_base_material.category IS '类型：1-原材料 2-半成品 3-成品 4-辅料';
COMMENT ON COLUMN t_base_material.safety_stock IS '安全库存';
COMMENT ON COLUMN t_base_material.price IS '参考单价';
COMMENT ON COLUMN t_base_material.status IS '状态：1-启用 0-禁用';
COMMENT ON COLUMN t_base_material.remark IS '备注';
COMMENT ON COLUMN t_base_material.create_time IS '创建时间';
COMMENT ON COLUMN t_base_material.update_time IS '更新时间';
COMMENT ON COLUMN t_base_material.create_by IS '创建人';
COMMENT ON COLUMN t_base_material.update_by IS '更新人';
COMMENT ON COLUMN t_base_material.deleted IS '逻辑删除';

CREATE TABLE t_bom_header (
    id                  BIGINT        NOT NULL PRIMARY KEY,
    bom_no              VARCHAR(30)   NOT NULL,
    product_material_id BIGINT        NOT NULL,
    version             VARCHAR(20)   DEFAULT '1.0',
    status              SMALLINT      NOT NULL DEFAULT 1,
    create_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by           BIGINT        DEFAULT NULL,
    update_by           BIGINT        DEFAULT NULL,
    deleted             SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_bom_no UNIQUE (bom_no)
);

COMMENT ON TABLE t_bom_header IS 'BOM头表';
COMMENT ON COLUMN t_bom_header.id IS '主键';
COMMENT ON COLUMN t_bom_header.bom_no IS 'BOM编号';
COMMENT ON COLUMN t_bom_header.product_material_id IS '成品物料ID';
COMMENT ON COLUMN t_bom_header.version IS '版本号';
COMMENT ON COLUMN t_bom_header.status IS '状态：1-启用 0-禁用';
COMMENT ON COLUMN t_bom_header.create_time IS '创建时间';
COMMENT ON COLUMN t_bom_header.update_time IS '更新时间';
COMMENT ON COLUMN t_bom_header.create_by IS '创建人';
COMMENT ON COLUMN t_bom_header.update_by IS '更新人';
COMMENT ON COLUMN t_bom_header.deleted IS '逻辑删除';

CREATE INDEX idx_product ON t_bom_header(product_material_id);

CREATE TABLE t_bom_detail (
    id          BIGINT        NOT NULL PRIMARY KEY,
    bom_id      BIGINT        NOT NULL,
    material_id BIGINT        NOT NULL,
    quantity    DECIMAL(18,4) NOT NULL,
    unit        VARCHAR(20)   DEFAULT NULL,
    remark      VARCHAR(200)  DEFAULT NULL,
    create_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_bom_detail IS 'BOM明细表';
COMMENT ON COLUMN t_bom_detail.id IS '主键';
COMMENT ON COLUMN t_bom_detail.bom_id IS '关联 t_bom_header.id';
COMMENT ON COLUMN t_bom_detail.material_id IS '原材料ID';
COMMENT ON COLUMN t_bom_detail.quantity IS '用量';
COMMENT ON COLUMN t_bom_detail.unit IS '单位';
COMMENT ON COLUMN t_bom_detail.remark IS '备注';
COMMENT ON COLUMN t_bom_detail.create_time IS '创建时间';
COMMENT ON COLUMN t_bom_detail.update_time IS '更新时间';

CREATE INDEX idx_bom_id ON t_bom_detail(bom_id);

CREATE TABLE t_base_customer (
    id          BIGINT       NOT NULL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    contact     VARCHAR(50)  DEFAULT NULL,
    phone       VARCHAR(30)  DEFAULT NULL,
    address     VARCHAR(300) DEFAULT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_code UNIQUE (code)
);

COMMENT ON TABLE t_base_customer IS '客户表';
COMMENT ON COLUMN t_base_customer.id IS '客户ID';
COMMENT ON COLUMN t_base_customer.code IS '客户编码';
COMMENT ON COLUMN t_base_customer.name IS '客户名称';
COMMENT ON COLUMN t_base_customer.contact IS '联系人';
COMMENT ON COLUMN t_base_customer.phone IS '联系电话';
COMMENT ON COLUMN t_base_customer.address IS '地址';
COMMENT ON COLUMN t_base_customer.status IS '状态';
COMMENT ON COLUMN t_base_customer.create_time IS '创建时间';
COMMENT ON COLUMN t_base_customer.update_time IS '更新时间';
COMMENT ON COLUMN t_base_customer.create_by IS '创建人';
COMMENT ON COLUMN t_base_customer.update_by IS '更新人';
COMMENT ON COLUMN t_base_customer.deleted IS '逻辑删除';

CREATE TABLE t_base_supplier (
    id          BIGINT       NOT NULL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    contact     VARCHAR(50)  DEFAULT NULL,
    phone       VARCHAR(30)  DEFAULT NULL,
    address     VARCHAR(300) DEFAULT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_code UNIQUE (code)
);

COMMENT ON TABLE t_base_supplier IS '供应商表';
COMMENT ON COLUMN t_base_supplier.id IS '供应商ID';
COMMENT ON COLUMN t_base_supplier.code IS '供应商编码';
COMMENT ON COLUMN t_base_supplier.name IS '供应商名称';
COMMENT ON COLUMN t_base_supplier.contact IS '联系人';
COMMENT ON COLUMN t_base_supplier.phone IS '联系电话';
COMMENT ON COLUMN t_base_supplier.address IS '地址';
COMMENT ON COLUMN t_base_supplier.status IS '状态';
COMMENT ON COLUMN t_base_supplier.create_time IS '创建时间';
COMMENT ON COLUMN t_base_supplier.update_time IS '更新时间';
COMMENT ON COLUMN t_base_supplier.create_by IS '创建人';
COMMENT ON COLUMN t_base_supplier.update_by IS '更新人';
COMMENT ON COLUMN t_base_supplier.deleted IS '逻辑删除';

CREATE TABLE t_base_warehouse (
    id          BIGINT       NOT NULL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    address     VARCHAR(300) DEFAULT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_code UNIQUE (code)
);

COMMENT ON TABLE t_base_warehouse IS '仓库表';
COMMENT ON COLUMN t_base_warehouse.id IS '仓库ID';
COMMENT ON COLUMN t_base_warehouse.code IS '仓库编码';
COMMENT ON COLUMN t_base_warehouse.name IS '仓库名称';
COMMENT ON COLUMN t_base_warehouse.address IS '仓库地址';
COMMENT ON COLUMN t_base_warehouse.status IS '状态';
COMMENT ON COLUMN t_base_warehouse.create_time IS '创建时间';
COMMENT ON COLUMN t_base_warehouse.update_time IS '更新时间';
COMMENT ON COLUMN t_base_warehouse.create_by IS '创建人';
COMMENT ON COLUMN t_base_warehouse.update_by IS '更新人';
COMMENT ON COLUMN t_base_warehouse.deleted IS '逻辑删除';

CREATE TABLE t_base_employee (
    id          BIGINT       NOT NULL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    gender      SMALLINT     DEFAULT NULL,
    phone       VARCHAR(30)  DEFAULT NULL,
    dept        VARCHAR(50)  DEFAULT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_code UNIQUE (code)
);

COMMENT ON TABLE t_base_employee IS '员工表';
COMMENT ON COLUMN t_base_employee.id IS '员工ID';
COMMENT ON COLUMN t_base_employee.code IS '员工编码';
COMMENT ON COLUMN t_base_employee.name IS '姓名';
COMMENT ON COLUMN t_base_employee.gender IS '性别：1-男 2-女';
COMMENT ON COLUMN t_base_employee.phone IS '手机号';
COMMENT ON COLUMN t_base_employee.dept IS '部门';
COMMENT ON COLUMN t_base_employee.status IS '状态';
COMMENT ON COLUMN t_base_employee.create_time IS '创建时间';
COMMENT ON COLUMN t_base_employee.update_time IS '更新时间';
COMMENT ON COLUMN t_base_employee.create_by IS '创建人';
COMMENT ON COLUMN t_base_employee.update_by IS '更新人';
COMMENT ON COLUMN t_base_employee.deleted IS '逻辑删除';

-- 公司账户（合并自 V9 + V15）
CREATE TABLE t_company_account (
    id            BIGINT        NOT NULL PRIMARY KEY,
    bank_name     VARCHAR(100)  DEFAULT NULL,
    branch_name   VARCHAR(100)  DEFAULT NULL,
    account_no    VARCHAR(50)   DEFAULT NULL,
    account_name  VARCHAR(100)  DEFAULT NULL,
    currency      VARCHAR(10)   DEFAULT 'CNY',
    account_type  VARCHAR(20)   NOT NULL DEFAULT 'BOTH',
    status        INT           DEFAULT 1,
    remark        VARCHAR(255)  DEFAULT NULL,
    create_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by     BIGINT        DEFAULT NULL,
    update_by     BIGINT        DEFAULT NULL
);

COMMENT ON TABLE t_company_account IS '公司账户表';
COMMENT ON COLUMN t_company_account.id IS '主键';
COMMENT ON COLUMN t_company_account.bank_name IS '银行名称';
COMMENT ON COLUMN t_company_account.branch_name IS '支行名称';
COMMENT ON COLUMN t_company_account.account_no IS '账号';
COMMENT ON COLUMN t_company_account.account_name IS '户名';
COMMENT ON COLUMN t_company_account.currency IS '币种';
COMMENT ON COLUMN t_company_account.account_type IS 'RECEIVE收款/PAY付款/BOTH均可';
COMMENT ON COLUMN t_company_account.status IS '1启用 0禁用';
COMMENT ON COLUMN t_company_account.remark IS '备注';
COMMENT ON COLUMN t_company_account.create_time IS '创建时间';
COMMENT ON COLUMN t_company_account.update_time IS '更新时间';
COMMENT ON COLUMN t_company_account.create_by IS '创建人';
COMMENT ON COLUMN t_company_account.update_by IS '更新人';

-- 客户/供应商银行账户（合并自 V13 + V15）
CREATE TABLE t_counterparty_account (
    id            BIGINT        NOT NULL PRIMARY KEY,
    owner_type    VARCHAR(20)   NOT NULL,
    owner_id      BIGINT        NOT NULL,
    bank_name     VARCHAR(100)  DEFAULT NULL,
    branch_name   VARCHAR(100)  DEFAULT NULL,
    account_no    VARCHAR(50)   DEFAULT NULL,
    account_name  VARCHAR(100)  DEFAULT NULL,
    currency      VARCHAR(10)   DEFAULT 'CNY',
    account_type  VARCHAR(20)   DEFAULT 'BOTH',
    status        INT           DEFAULT 1,
    remark        VARCHAR(255)  DEFAULT NULL,
    create_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by     BIGINT        DEFAULT NULL,
    update_by     BIGINT        DEFAULT NULL
);

COMMENT ON TABLE t_counterparty_account IS '往来方账户表';
COMMENT ON COLUMN t_counterparty_account.id IS '主键';
COMMENT ON COLUMN t_counterparty_account.owner_type IS 'CUSTOMER / SUPPLIER';
COMMENT ON COLUMN t_counterparty_account.owner_id IS '客户/供应商ID';
COMMENT ON COLUMN t_counterparty_account.bank_name IS '银行名称';
COMMENT ON COLUMN t_counterparty_account.branch_name IS '支行';
COMMENT ON COLUMN t_counterparty_account.account_no IS '账号';
COMMENT ON COLUMN t_counterparty_account.account_name IS '户名';
COMMENT ON COLUMN t_counterparty_account.currency IS '币种';
COMMENT ON COLUMN t_counterparty_account.account_type IS 'RECEIVE / PAY / BOTH';
COMMENT ON COLUMN t_counterparty_account.status IS '1启用 0禁用';
COMMENT ON COLUMN t_counterparty_account.remark IS '备注';
COMMENT ON COLUMN t_counterparty_account.create_time IS '创建时间';
COMMENT ON COLUMN t_counterparty_account.update_time IS '更新时间';
COMMENT ON COLUMN t_counterparty_account.create_by IS '创建人';
COMMENT ON COLUMN t_counterparty_account.update_by IS '更新人';

-- ==================== 销售管理 ====================

CREATE TABLE t_sales_order (
    id              BIGINT         NOT NULL PRIMARY KEY,
    order_no        VARCHAR(30)    NOT NULL,
    customer_id     BIGINT         NOT NULL,
    order_date      DATE           NOT NULL,
    delivery_date   DATE           DEFAULT NULL,
    customer_po_no  VARCHAR(100)   DEFAULT NULL,
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

COMMENT ON TABLE t_sales_order IS '销售订单主表';
COMMENT ON COLUMN t_sales_order.id IS '订单ID';
COMMENT ON COLUMN t_sales_order.order_no IS '订单号 (SO + yyyyMMdd + 4位流水)';
COMMENT ON COLUMN t_sales_order.customer_id IS '客户ID';
COMMENT ON COLUMN t_sales_order.order_date IS '订单日期';
COMMENT ON COLUMN t_sales_order.delivery_date IS '交货日期';
COMMENT ON COLUMN t_sales_order.customer_po_no IS '客户采购订单号';
COMMENT ON COLUMN t_sales_order.total_amount IS '总金额';
COMMENT ON COLUMN t_sales_order.status IS '状态：DRAFT/SUBMITTED/APPROVED/PARTIAL_SHIPPED/COMPLETED/CLOSED';
COMMENT ON COLUMN t_sales_order.remark IS '备注';
COMMENT ON COLUMN t_sales_order.create_time IS '创建时间';
COMMENT ON COLUMN t_sales_order.update_time IS '更新时间';
COMMENT ON COLUMN t_sales_order.create_by IS '创建人';
COMMENT ON COLUMN t_sales_order.update_by IS '更新人';
COMMENT ON COLUMN t_sales_order.deleted IS '逻辑删除';

CREATE INDEX idx_customer ON t_sales_order(customer_id);
CREATE INDEX idx_status ON t_sales_order(status);

CREATE TABLE t_sales_order_item (
    id          BIGINT         NOT NULL PRIMARY KEY,
    order_id    BIGINT         NOT NULL,
    line_no     INT            NOT NULL,
    material_id BIGINT         NOT NULL,
    quantity    DECIMAL(18,2)  NOT NULL,
    unit        VARCHAR(20)    NOT NULL,
    price       DECIMAL(18,2)  NOT NULL,
    amount      DECIMAL(18,2)  NOT NULL,
    shipped_qty DECIMAL(18,2)  DEFAULT 0,
    create_time TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_sales_order_item IS '销售订单明细表';
COMMENT ON COLUMN t_sales_order_item.id IS '明细ID';
COMMENT ON COLUMN t_sales_order_item.order_id IS '订单ID';
COMMENT ON COLUMN t_sales_order_item.line_no IS '行号';
COMMENT ON COLUMN t_sales_order_item.material_id IS '物料ID';
COMMENT ON COLUMN t_sales_order_item.quantity IS '数量';
COMMENT ON COLUMN t_sales_order_item.unit IS '单位';
COMMENT ON COLUMN t_sales_order_item.price IS '单价';
COMMENT ON COLUMN t_sales_order_item.amount IS '金额';
COMMENT ON COLUMN t_sales_order_item.shipped_qty IS '已发货数量';
COMMENT ON COLUMN t_sales_order_item.create_time IS '创建时间';
COMMENT ON COLUMN t_sales_order_item.update_time IS '更新时间';

CREATE INDEX idx_order_id ON t_sales_order_item(order_id);

CREATE TABLE t_sales_delivery (
    id            BIGINT         NOT NULL PRIMARY KEY,
    delivery_no   VARCHAR(30)    NOT NULL,
    order_id      BIGINT         NOT NULL,
    warehouse_id  BIGINT         NOT NULL,
    delivery_date DATE           DEFAULT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    remark        VARCHAR(500)   DEFAULT NULL,
    create_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by     BIGINT         DEFAULT NULL,
    update_by     BIGINT         DEFAULT NULL,
    deleted       SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_delivery_no UNIQUE (delivery_no)
);

COMMENT ON TABLE t_sales_delivery IS '发货单主表';
COMMENT ON COLUMN t_sales_delivery.id IS '发货单ID';
COMMENT ON COLUMN t_sales_delivery.delivery_no IS '发货单号';
COMMENT ON COLUMN t_sales_delivery.order_id IS '销售订单ID';
COMMENT ON COLUMN t_sales_delivery.warehouse_id IS '仓库ID';
COMMENT ON COLUMN t_sales_delivery.delivery_date IS '发货日期';
COMMENT ON COLUMN t_sales_delivery.status IS '状态：DRAFT/CONFIRMED';
COMMENT ON COLUMN t_sales_delivery.remark IS '备注';
COMMENT ON COLUMN t_sales_delivery.create_time IS '创建时间';
COMMENT ON COLUMN t_sales_delivery.update_time IS '更新时间';
COMMENT ON COLUMN t_sales_delivery.create_by IS '创建人';
COMMENT ON COLUMN t_sales_delivery.update_by IS '更新人';
COMMENT ON COLUMN t_sales_delivery.deleted IS '逻辑删除';

CREATE INDEX idx_order_id ON t_sales_delivery(order_id);

CREATE TABLE t_sales_delivery_item (
    id            BIGINT         NOT NULL PRIMARY KEY,
    delivery_id   BIGINT         NOT NULL,
    order_item_id BIGINT         NOT NULL,
    material_id   BIGINT         NOT NULL,
    quantity      DECIMAL(18,2)  NOT NULL,
    create_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_sales_delivery_item IS '发货单明细表';
COMMENT ON COLUMN t_sales_delivery_item.id IS '明细ID';
COMMENT ON COLUMN t_sales_delivery_item.delivery_id IS '发货单ID';
COMMENT ON COLUMN t_sales_delivery_item.order_item_id IS '订单明细ID';
COMMENT ON COLUMN t_sales_delivery_item.material_id IS '物料ID';
COMMENT ON COLUMN t_sales_delivery_item.quantity IS '实际发货数量';
COMMENT ON COLUMN t_sales_delivery_item.create_time IS '创建时间';
COMMENT ON COLUMN t_sales_delivery_item.update_time IS '更新时间';

CREATE INDEX idx_delivery_id ON t_sales_delivery_item(delivery_id);

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
    applicant_id    BIGINT         DEFAULT NULL,
    req_date        DATE           DEFAULT NULL,
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
COMMENT ON COLUMN t_pur_requisition.applicant_id IS '申请人（员工ID）';
COMMENT ON COLUMN t_pur_requisition.req_date IS '申请日期';
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
    requisition_id  BIGINT         DEFAULT NULL,
    order_date      DATE           NOT NULL,
    delivery_date   DATE           DEFAULT NULL,
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
COMMENT ON COLUMN t_pur_order.requisition_id IS '采购申请ID';
COMMENT ON COLUMN t_pur_order.order_date IS '订单日期';
COMMENT ON COLUMN t_pur_order.delivery_date IS '交货日期';
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

-- 库存调拨单（V19）
CREATE TABLE t_inv_transfer (
    id                BIGINT        NOT NULL PRIMARY KEY,
    transfer_no       VARCHAR(50)   NOT NULL,
    from_warehouse_id BIGINT        NOT NULL,
    to_warehouse_id   BIGINT        NOT NULL,
    transfer_date     DATE          DEFAULT NULL,
    status            VARCHAR(20)   DEFAULT 'DRAFT',
    remark            VARCHAR(500)  DEFAULT NULL,
    create_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by         BIGINT        DEFAULT NULL,
    update_by         BIGINT        DEFAULT NULL,
    deleted           SMALLINT      NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_inv_transfer IS '库存调拨单';
COMMENT ON COLUMN t_inv_transfer.id IS '调拨单ID';
COMMENT ON COLUMN t_inv_transfer.transfer_no IS '调拨单号';
COMMENT ON COLUMN t_inv_transfer.from_warehouse_id IS '调出仓库ID';
COMMENT ON COLUMN t_inv_transfer.to_warehouse_id IS '调入仓库ID';
COMMENT ON COLUMN t_inv_transfer.transfer_date IS '调拨日期';
COMMENT ON COLUMN t_inv_transfer.status IS 'DRAFT/CONFIRMED';
COMMENT ON COLUMN t_inv_transfer.remark IS '备注';
COMMENT ON COLUMN t_inv_transfer.create_time IS '创建时间';
COMMENT ON COLUMN t_inv_transfer.update_time IS '更新时间';
COMMENT ON COLUMN t_inv_transfer.create_by IS '创建人';
COMMENT ON COLUMN t_inv_transfer.update_by IS '更新人';
COMMENT ON COLUMN t_inv_transfer.deleted IS '逻辑删除';

CREATE TABLE t_inv_transfer_item (
    id            BIGINT        NOT NULL PRIMARY KEY,
    transfer_id   BIGINT        NOT NULL,
    material_id   BIGINT        NOT NULL,
    quantity      DECIMAL(18,4) NOT NULL,
    create_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by     BIGINT        DEFAULT NULL,
    update_by     BIGINT        DEFAULT NULL
);

COMMENT ON TABLE t_inv_transfer_item IS '库存调拨明细表';
COMMENT ON COLUMN t_inv_transfer_item.id IS '调拨明细ID';
COMMENT ON COLUMN t_inv_transfer_item.transfer_id IS '调拨单ID';
COMMENT ON COLUMN t_inv_transfer_item.material_id IS '物料ID';
COMMENT ON COLUMN t_inv_transfer_item.quantity IS '调拨数量';
COMMENT ON COLUMN t_inv_transfer_item.create_time IS '创建时间';
COMMENT ON COLUMN t_inv_transfer_item.update_time IS '更新时间';
COMMENT ON COLUMN t_inv_transfer_item.create_by IS '创建人';
COMMENT ON COLUMN t_inv_transfer_item.update_by IS '更新人';

CREATE INDEX idx_transfer_id ON t_inv_transfer_item(transfer_id);

-- ==================== 财务管理 ====================

CREATE TABLE t_fin_receivable (
    id                BIGINT         NOT NULL PRIMARY KEY,
    delivery_id       BIGINT         NOT NULL,
    customer_id       BIGINT         NOT NULL,
    receivable_amount DECIMAL(18,2)  NOT NULL,
    received_amount   DECIMAL(18,2)  DEFAULT 0,
    due_date          DATE           DEFAULT NULL,
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
COMMENT ON COLUMN t_fin_receivable.due_date IS '应收日期';
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
    due_date        DATE           DEFAULT NULL,
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
COMMENT ON COLUMN t_fin_payable.due_date IS '应付日期';
COMMENT ON COLUMN t_fin_payable.status IS '状态：UNPAID/PARTIAL_PAID/PAID';
COMMENT ON COLUMN t_fin_payable.create_time IS '创建时间';
COMMENT ON COLUMN t_fin_payable.update_time IS '更新时间';
COMMENT ON COLUMN t_fin_payable.create_by IS '创建人';
COMMENT ON COLUMN t_fin_payable.update_by IS '更新人';
COMMENT ON COLUMN t_fin_payable.deleted IS '逻辑删除';

CREATE INDEX idx_supplier ON t_fin_payable(supplier_id);
CREATE INDEX idx_status ON t_fin_payable(status);

-- 收付款单（合并 V8 reconciled_amount）
CREATE TABLE t_fin_payment (
    id                  BIGINT         NOT NULL PRIMARY KEY,
    payment_no          VARCHAR(30)    NOT NULL,
    type                VARCHAR(10)    NOT NULL,
    counterparty_id     BIGINT         NOT NULL,
    amount              DECIMAL(18,2)  NOT NULL,
    company_account_id  BIGINT         DEFAULT NULL,
    reconciled_amount   DECIMAL(18,2)  DEFAULT 0,
    payment_date        DATE           NOT NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    remark              VARCHAR(500)   DEFAULT NULL,
    create_time         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by           BIGINT         DEFAULT NULL,
    update_by           BIGINT         DEFAULT NULL,
    deleted             SMALLINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_payment_no UNIQUE (payment_no)
);

COMMENT ON TABLE t_fin_payment IS '收付款单';
COMMENT ON COLUMN t_fin_payment.id IS '收付款单ID';
COMMENT ON COLUMN t_fin_payment.payment_no IS '收付款单号';
COMMENT ON COLUMN t_fin_payment.type IS '类型：RECEIVE/PAY';
COMMENT ON COLUMN t_fin_payment.counterparty_id IS '对方ID（客户/供应商）';
COMMENT ON COLUMN t_fin_payment.amount IS '金额';
COMMENT ON COLUMN t_fin_payment.company_account_id IS '公司账户ID';
COMMENT ON COLUMN t_fin_payment.reconciled_amount IS '已核销金额';
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

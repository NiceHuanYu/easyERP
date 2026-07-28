-- =====================================================
-- V1__init_schema.sql (PostgreSQL)
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

-- ==================== 销售管理 ====================

CREATE TABLE t_sales_order (
    id              BIGINT         NOT NULL PRIMARY KEY,
    order_no        VARCHAR(30)    NOT NULL,
    customer_id     BIGINT         NOT NULL,
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

COMMENT ON TABLE t_sales_order IS '销售订单主表';
COMMENT ON COLUMN t_sales_order.id IS '订单ID';
COMMENT ON COLUMN t_sales_order.order_no IS '订单号 (SO + yyyyMMdd + 4位流水)';
COMMENT ON COLUMN t_sales_order.customer_id IS '客户ID';
COMMENT ON COLUMN t_sales_order.order_date IS '订单日期';
COMMENT ON COLUMN t_sales_order.delivery_date IS '交货日期';
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

-- =====================================================
-- V2__init_data.sql — 全部初始化数据（合并自 V3/V4/V12/V16/V18/V20/V21）
-- 管理员用户由 DataInitializer 在启动时创建
-- =====================================================

-- ==================== 角色 ====================
INSERT INTO t_sys_role (id, name, code, status, create_time, update_time) VALUES
(1,  '管理员',   'admin',      1, NOW(), NOW()),
(2,  '销售',     'sales',      1, NOW(), NOW()),
(3,  '采购',     'purchase',   1, NOW(), NOW()),
(4,  '仓库',     'warehouse',  1, NOW(), NOW()),
(5,  '生产',     'production', 1, NOW(), NOW()),
(6,  '质量',     'quality',    1, NOW(), NOW()),
(7,  '财务',     'finance',    1, NOW(), NOW());

-- ==================== 权限（菜单 + 按钮） ====================

-- 一级菜单
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, icon, sort, status, create_time, update_time) VALUES
(1, 0, '系统管理', NULL, 1, '/system', 'Setting', 1, 1, NOW(), NOW()),
(2, 0, '基础数据', NULL, 1, '/base-data', 'Document', 2, 1, NOW(), NOW()),
(3, 0, '销售管理', NULL, 1, '/sales', 'Sell', 3, 1, NOW(), NOW()),
(4, 0, '生产管理', NULL, 1, '/production', 'Cpu', 4, 1, NOW(), NOW()),
(5, 0, '采购管理', NULL, 1, '/purchase', 'ShoppingCart', 5, 1, NOW(), NOW()),
(6, 0, '库存管理', NULL, 1, '/inventory', 'Box', 6, 1, NOW(), NOW()),
(7, 0, '财务管理', NULL, 1, '/finance', 'Money', 7, 1, NOW(), NOW());

-- ===== 系统管理 =====
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(11, 1, '用户管理', 'system:user:view', 1, '/system/users', 1, 1, NOW(), NOW()),
(12, 1, '角色管理', 'system:role:view', 1, '/system/roles', 2, 1, NOW(), NOW()),
(13, 1, '数据字典', 'system:dict:view', 1, '/system/dicts', 3, 1, NOW(), NOW());

INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(111, 11, '新增用户', 'system:user:create', 2, 1, 1, NOW(), NOW()),
(112, 11, '编辑用户', 'system:user:edit', 2, 2, 1, NOW(), NOW()),
(113, 11, '删除用户', 'system:user:delete', 2, 3, 1, NOW(), NOW()),
(121, 12, '新增角色', 'system:role:create', 2, 1, 1, NOW(), NOW()),
(122, 12, '编辑角色', 'system:role:edit', 2, 2, 1, NOW(), NOW()),
(123, 12, '删除角色', 'system:role:delete', 2, 3, 1, NOW(), NOW()),
(131, 13, '新增字典', 'system:dict:create', 2, 1, 1, NOW(), NOW()),
(132, 13, '编辑字典', 'system:dict:edit', 2, 2, 1, NOW(), NOW()),
(133, 13, '删除字典', 'system:dict:delete', 2, 3, 1, NOW(), NOW());

-- ===== 基础数据 =====
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(21, 2, '物料管理',   'base-data:material:view',         1, '/base-data/materials',  1, 1, NOW(), NOW()),
(22, 2, 'BOM管理',    'base-data:bom:view',              1, '/base-data/boms',       2, 1, NOW(), NOW()),
(23, 2, '客户管理',   'base-data:customer:view',         1, '/base-data/customers',  3, 1, NOW(), NOW()),
(24, 2, '供应商管理', 'base-data:supplier:view',         1, '/base-data/suppliers',  4, 1, NOW(), NOW()),
(25, 2, '仓库管理',   'base-data:warehouse:view',        1, '/base-data/warehouses', 5, 1, NOW(), NOW()),
(26, 2, '员工管理',   'base-data:employee:view',         1, '/base-data/employees',  6, 1, NOW(), NOW()),
(27, 2, '公司账户',   'base-data:company-account:view',  1, '/base-data/company-accounts', 7, 1, NOW(), NOW());

INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(211, 21, '新增物料',   'base-data:material:create',         2, 1, 1, NOW(), NOW()),
(212, 21, '编辑物料',   'base-data:material:edit',           2, 2, 1, NOW(), NOW()),
(213, 21, '删除物料',   'base-data:material:delete',         2, 3, 1, NOW(), NOW()),
(221, 22, '新增BOM',    'base-data:bom:create',              2, 1, 1, NOW(), NOW()),
(222, 22, '编辑BOM',    'base-data:bom:edit',                2, 2, 1, NOW(), NOW()),
(223, 22, '删除BOM',    'base-data:bom:delete',              2, 3, 1, NOW(), NOW()),
(231, 23, '新增客户',   'base-data:customer:create',         2, 1, 1, NOW(), NOW()),
(232, 23, '编辑客户',   'base-data:customer:edit',           2, 2, 1, NOW(), NOW()),
(233, 23, '删除客户',   'base-data:customer:delete',         2, 3, 1, NOW(), NOW()),
(241, 24, '新增供应商', 'base-data:supplier:create',         2, 1, 1, NOW(), NOW()),
(242, 24, '编辑供应商', 'base-data:supplier:edit',           2, 2, 1, NOW(), NOW()),
(243, 24, '删除供应商', 'base-data:supplier:delete',         2, 3, 1, NOW(), NOW()),
(251, 25, '新增仓库',   'base-data:warehouse:create',        2, 1, 1, NOW(), NOW()),
(252, 25, '编辑仓库',   'base-data:warehouse:edit',          2, 2, 1, NOW(), NOW()),
(253, 25, '删除仓库',   'base-data:warehouse:delete',        2, 3, 1, NOW(), NOW()),
(261, 26, '新增员工',   'base-data:employee:create',         2, 1, 1, NOW(), NOW()),
(262, 26, '编辑员工',   'base-data:employee:edit',           2, 2, 1, NOW(), NOW()),
(263, 26, '删除员工',   'base-data:employee:delete',         2, 3, 1, NOW(), NOW()),
(271, 27, '新增公司账户','base-data:company-account:create', 2, 1, 1, NOW(), NOW()),
(272, 27, '编辑公司账户','base-data:company-account:edit',   2, 2, 1, NOW(), NOW()),
(273, 27, '删除公司账户','base-data:company-account:delete', 2, 3, 1, NOW(), NOW());

-- ===== 销售管理 =====
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(31, 3, '销售订单', 'sales:order:view',    1, '/sales/orders',     1, 1, NOW(), NOW()),
(32, 3, '发货管理', 'delivery:order:view', 1, '/sales/deliveries', 2, 1, NOW(), NOW());

INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(311, 31, '创建订单', 'sales:order:create',    2, 1, 1, NOW(), NOW()),
(312, 31, '编辑订单', 'sales:order:edit',      2, 2, 1, NOW(), NOW()),
(313, 31, '删除订单', 'sales:order:delete',    2, 3, 1, NOW(), NOW()),
(314, 31, '提交订单', 'sales:order:submit',    2, 4, 1, NOW(), NOW()),
(315, 31, '审核订单', 'sales:order:approve',   2, 5, 1, NOW(), NOW()),
(321, 32, '创建发货', 'delivery:order:create', 2, 1, 1, NOW(), NOW()),
(322, 32, '编辑发货', 'delivery:order:edit',   2, 2, 1, NOW(), NOW()),
(323, 32, '删除发货', 'delivery:order:delete', 2, 3, 1, NOW(), NOW()),
(324, 32, '确认发货', 'delivery:order:approve',2, 4, 1, NOW(), NOW());

-- ===== 生产管理 =====
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(41, 4, '生产工单', 'production:order:view', 1, '/production/orders',     1, 1, NOW(), NOW()),
(42, 4, '领料管理', 'production:order:view', 1, '/production/pickings',   2, 1, NOW(), NOW()),
(43, 4, '完工入库', 'production:order:view', 1, '/production/finishings', 3, 1, NOW(), NOW());

INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(411, 41, '创建工单',     'production:order:create',     2, 1, 1, NOW(), NOW()),
(412, 41, '编辑工单',     'production:order:edit',       2, 2, 1, NOW(), NOW()),
(413, 41, '删除工单',     'production:order:delete',     2, 3, 1, NOW(), NOW()),
(414, 41, '下达工单',     'production:order:release',    2, 4, 1, NOW(), NOW()),
(415, 41, '完工',         'production:order:finish',     2, 5, 1, NOW(), NOW()),
(421, 42, '确认领料',     'production:picking:confirm',  2, 1, 1, NOW(), NOW()),
(431, 43, '确认完工入库', 'production:finish:confirm',   2, 1, 1, NOW(), NOW());

-- ===== 采购管理 =====
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(51, 5, '采购申请', 'purchase:order:view', 1, '/purchase/requisitions', 1, 1, NOW(), NOW()),
(52, 5, '采购订单', 'purchase:order:view', 1, '/purchase/orders',       2, 1, NOW(), NOW()),
(53, 5, '收货管理', 'purchase:order:view', 1, '/purchase/receivings',   3, 1, NOW(), NOW());

INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(511, 51, '创建申请', 'purchase:order:create',  2, 1, 1, NOW(), NOW()),
(512, 52, '创建订单', 'purchase:order:create',  2, 2, 1, NOW(), NOW()),
(513, 52, '编辑订单', 'purchase:order:edit',    2, 3, 1, NOW(), NOW()),
(514, 52, '删除订单', 'purchase:order:delete',  2, 4, 1, NOW(), NOW()),
(521, 53, '确认收货', 'purchase:order:approve', 2, 1, 1, NOW(), NOW());

-- ===== 库存管理 =====
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(61, 6, '库存查询', 'inventory:stock:view', 1, '/inventory/stock',        1, 1, NOW(), NOW()),
(62, 6, '库存流水', 'inventory:stock:view', 1, '/inventory/transactions', 2, 1, NOW(), NOW()),
(63, 6, '库存调拨', 'inventory:stock:view', 1, '/inventory/transfers',    3, 1, NOW(), NOW());

INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(611, 61, '导出库存', 'inventory:stock:export', 2, 4, 1, NOW(), NOW()),
(621, 62, '导出流水', 'inventory:stock:export', 2, 4, 1, NOW(), NOW());

-- ===== 财务管理 =====
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(71, 7, '应收台账', 'finance:order:view', 1, '/finance/receivables', 1, 1, NOW(), NOW()),
(72, 7, '应付台账', 'finance:order:view', 1, '/finance/payables',    2, 1, NOW(), NOW()),
(73, 7, '收付款',   'finance:order:view', 1, '/finance/payments',    3, 1, NOW(), NOW()),
(28, 7, '银行账户', 'finance:bank-account:view', 1, '/finance/bank-accounts', 4, 1, NOW(), NOW());

INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(711, 73, '创建收付款',     'finance:order:create',         2, 1, 1, NOW(), NOW()),
(712, 73, '编辑收付款',     'finance:order:edit',           2, 2, 1, NOW(), NOW()),
(713, 73, '删除收付款',     'finance:order:delete',         2, 3, 1, NOW(), NOW()),
(714, 73, '确认收付款',     'finance:order:approve',        2, 4, 1, NOW(), NOW()),
(281, 28, '新增银行账户',   'finance:bank-account:create',  2, 1, 1, NOW(), NOW()),
(282, 28, '编辑银行账户',   'finance:bank-account:edit',    2, 2, 1, NOW(), NOW()),
(283, 28, '删除银行账户',   'finance:bank-account:delete',  2, 3, 1, NOW(), NOW());

-- ==================== 角色权限关联 ====================

-- 管理员（role_id=1）：拥有全部权限
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 1000, 1, id
FROM t_sys_permission WHERE status = 1;

-- 销售角色（role_id=2）：销售 + 发货
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 2000, 2, id FROM t_sys_permission
WHERE code IN ('sales:order:view','sales:order:create','sales:order:edit','sales:order:delete','sales:order:submit','sales:order:approve',
               'delivery:order:view','delivery:order:create','delivery:order:edit','delivery:order:delete','delivery:order:approve');

-- 采购角色（role_id=3）：采购
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 3000, 3, id FROM t_sys_permission
WHERE code IN ('purchase:order:view','purchase:order:create','purchase:order:edit','purchase:order:delete','purchase:order:approve');

-- 仓库角色（role_id=4）：库存 + 发货确认 + 收货确认 + 调拨 + 导出
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 4000, 4, id FROM t_sys_permission
WHERE code IN ('inventory:stock:view','inventory:stock:export',
               'delivery:order:view','delivery:order:approve',
               'purchase:order:view','purchase:order:approve');

-- 生产角色（role_id=5）：生产
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 5000, 5, id FROM t_sys_permission
WHERE code IN ('production:order:view','production:order:create','production:order:edit','production:order:delete',
               'production:order:release','production:order:finish',
               'production:picking:confirm','production:finish:confirm');

-- 质量角色（role_id=6）：查看生产 + 库存
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 6000, 6, id FROM t_sys_permission
WHERE code IN ('production:order:view','inventory:stock:view');

-- 财务角色（role_id=7）：财务
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 7000, 7, id FROM t_sys_permission
WHERE code IN ('finance:order:view','finance:order:create','finance:order:edit','finance:order:delete','finance:order:approve');

-- ==================== 数据字典 ====================

INSERT INTO t_sys_dict (id, name, code, status, create_time, update_time) VALUES
(1, '物料类型',     'material_category',    1, NOW(), NOW()),
(2, '单据状态',     'order_status',         1, NOW(), NOW()),
(3, '库存变动类型', 'inv_transaction_type', 1, NOW(), NOW());

INSERT INTO t_sys_dict_item (id, dict_id, label, value, sort, status, create_time, update_time) VALUES
-- 物料类型
(101, 1, '原材料', '1', 1, 1, NOW(), NOW()),
(102, 1, '半成品', '2', 2, 1, NOW(), NOW()),
(103, 1, '成品',   '3', 3, 1, NOW(), NOW()),
(104, 1, '辅料',   '4', 4, 1, NOW(), NOW()),
-- 单据状态（通用）
(201, 2, '草稿',   'DRAFT',    1, 1, NOW(), NOW()),
(202, 2, '已提交', 'SUBMITTED', 2, 1, NOW(), NOW()),
(203, 2, '已审核', 'APPROVED',  3, 1, NOW(), NOW()),
(204, 2, '已完成', 'COMPLETED', 4, 1, NOW(), NOW()),
(205, 2, '已关闭', 'CLOSED',    5, 1, NOW(), NOW()),
-- 库存变动类型
(301, 3, '采购入库',   'PURCHASE_IN',    1, 1, NOW(), NOW()),
(302, 3, '生产领料',   'PICKING_OUT',    2, 1, NOW(), NOW()),
(303, 3, '生产退料',   'PICKING_RETURN', 3, 1, NOW(), NOW()),
(304, 3, '完工入库',   'FINISH_IN',      4, 1, NOW(), NOW()),
(305, 3, '销售出库',   'SALES_OUT',      5, 1, NOW(), NOW()),
(306, 3, '调拨',       'TRANSFER',       6, 1, NOW(), NOW()),
(307, 3, '盘点调整',   'ADJUST',         7, 1, NOW(), NOW()),
(308, 3, '报废',       'SCRAP',          8, 1, NOW(), NOW()),
(309, 3, '调拨出库',   'TRANSFER_OUT',   9, 1, NOW(), NOW()),
(310, 3, '调拨入库',   'TRANSFER_IN',   10, 1, NOW(), NOW());

-- =====================================================
-- V3__init_data.sql — 初始化：管理员 / 角色 / 权限
-- =====================================================

-- 默认管理员 (admin / admin123)
-- 密码为 BCrypt 加密，若登录失败请在应用中用 Hutool BCrypt.hashpw("admin123") 重新生成
INSERT INTO t_sys_user (id, username, password, nickname, status, create_time, update_time)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 1, NOW(), NOW());

-- ==================== 角色 ====================
INSERT INTO t_sys_role (id, name, code, status, create_time, update_time) VALUES
(1,  '管理员',   'admin',      1, NOW(), NOW()),
(2,  '销售',     'sales',      1, NOW(), NOW()),
(3,  '采购',     'purchase',   1, NOW(), NOW()),
(4,  '仓库',     'warehouse',  1, NOW(), NOW()),
(5,  '生产',     'production', 1, NOW(), NOW()),
(6,  '质量',     'quality',    1, NOW(), NOW()),
(7,  '财务',     'finance',    1, NOW(), NOW());

-- 管理员拥有 admin 角色
INSERT INTO t_sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- ==================== 权限（菜单 + 按钮） ====================
-- 一级菜单
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, icon, sort, status, create_time, update_time) VALUES
(1,  0, '系统管理', NULL,               1, '/system',      'Setting',      1, 1, NOW(), NOW()),
(2,  0, '基础数据', NULL,               1, '/base',        'Document',     2, 1, NOW(), NOW()),
(3,  0, '销售管理', NULL,               1, '/sales',       'Sell',         3, 1, NOW(), NOW()),
(4,  0, '生产管理', NULL,               1, '/production',  'Cpu',          4, 1, NOW(), NOW()),
(5,  0, '采购管理', NULL,               1, '/purchase',    'ShoppingCart', 5, 1, NOW(), NOW()),
(6,  0, '库存管理', NULL,               1, '/inventory',   'Box',          6, 1, NOW(), NOW()),
(7,  0, '财务管理', NULL,               1, '/finance',     'Money',        7, 1, NOW(), NOW());

-- 系统管理子菜单
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(11, 1, '用户管理', 'system:user:list',   1, '/system/users',        1, 1, NOW(), NOW()),
(12, 1, '角色管理', 'system:role:list',   1, '/system/roles',        2, 1, NOW(), NOW()),
(13, 1, '数据字典', 'system:dict:list',   1, '/system/dicts',        3, 1, NOW(), NOW());

-- 系统管理按钮权限
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(111, 11, '新增用户', 'system:user:create',  2, 1, 1, NOW(), NOW()),
(112, 11, '编辑用户', 'system:user:update',  2, 2, 1, NOW(), NOW()),
(113, 11, '删除用户', 'system:user:delete',  2, 3, 1, NOW(), NOW()),
(114, 11, '查看用户', 'system:user:view',    2, 4, 1, NOW(), NOW()),
(121, 12, '新增角色', 'system:role:create',  2, 1, 1, NOW(), NOW()),
(122, 12, '编辑角色', 'system:role:update',  2, 2, 1, NOW(), NOW());

-- 基础数据子菜单
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(21, 2, '物料管理', 'base:material:list',  1, '/base/materials',   1, 1, NOW(), NOW()),
(22, 2, 'BOM管理',  'base:bom:list',       1, '/base/boms',        2, 1, NOW(), NOW()),
(23, 2, '客户管理', 'base:customer:list',  1, '/base/customers',   3, 1, NOW(), NOW()),
(24, 2, '供应商管理','base:supplier:list',  1, '/base/suppliers',   4, 1, NOW(), NOW()),
(25, 2, '仓库管理', 'base:warehouse:list', 1, '/base/warehouses',  5, 1, NOW(), NOW()),
(26, 2, '员工管理', 'base:employee:list',  1, '/base/employees',   6, 1, NOW(), NOW());

-- 基础数据按钮权限
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(211, 21, '新增物料', 'base:material:create', 2, 1, 1, NOW(), NOW()),
(212, 21, '编辑物料', 'base:material:update', 2, 2, 1, NOW(), NOW()),
(213, 21, '删除物料', 'base:material:delete', 2, 3, 1, NOW(), NOW());

-- 销售管理子菜单
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(31, 3, '销售订单', 'sales:order:list',   1, '/sales/orders',      1, 1, NOW(), NOW()),
(32, 3, '发货管理', 'sales:delivery:list', 1, '/sales/deliveries',  2, 1, NOW(), NOW());

-- 销售管理按钮权限
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(311, 31, '创建订单', 'sales:order:create', 2, 1, 1, NOW(), NOW()),
(312, 31, '编辑订单', 'sales:order:update', 2, 2, 1, NOW(), NOW()),
(313, 31, '提交订单', 'sales:order:submit', 2, 3, 1, NOW(), NOW()),
(314, 31, '审核订单', 'sales:order:approve',2, 4, 1, NOW(), NOW()),
(315, 31, '创建发货', 'sales:delivery:create',2, 5, 1, NOW(), NOW()),
(316, 31, '确认发货', 'sales:delivery:confirm',2,6, 1, NOW(), NOW());

-- 生产管理子菜单
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(41, 4, '生产工单', 'production:order:list',   1, '/production/orders',    1, 1, NOW(), NOW()),
(42, 4, '领料管理', 'production:picking:list', 1, '/production/pickings',  2, 1, NOW(), NOW()),
(43, 4, '完工入库', 'production:finish:list',  1, '/production/finishings',3, 1, NOW(), NOW());

-- 生产管理按钮
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(411, 41, '创建工单', 'production:order:create',  2, 1, 1, NOW(), NOW()),
(412, 41, '下达工单', 'production:order:release', 2, 2, 1, NOW(), NOW()),
(413, 42, '确认领料', 'production:picking:confirm',2, 3, 1, NOW(), NOW()),
(414, 43, '确认入库', 'production:finish:confirm', 2, 4, 1, NOW(), NOW());

-- 采购管理子菜单
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(51, 5, '采购申请', 'purchase:requisition:list', 1, '/purchase/requisitions', 1, 1, NOW(), NOW()),
(52, 5, '采购订单', 'purchase:order:list',       1, '/purchase/orders',       2, 1, NOW(), NOW()),
(53, 5, '收货管理', 'purchase:receiving:list',   1, '/purchase/receivings',   3, 1, NOW(), NOW());

-- 采购管理按钮
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(511, 51, '创建申请', 'purchase:requisition:create', 2, 1, 1, NOW(), NOW()),
(512, 52, '创建订单', 'purchase:order:create',       2, 2, 1, NOW(), NOW()),
(513, 53, '确认收货', 'purchase:receiving:confirm',  2, 3, 1, NOW(), NOW());

-- 库存管理子菜单
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(61, 6, '库存查询', 'inventory:stock:list',  1, '/inventory/stock',        1, 1, NOW(), NOW()),
(62, 6, '库存流水', 'inventory:transaction:list',1,'/inventory/transactions',2,1, NOW(), NOW());

-- 财务管理子菜单
INSERT INTO t_sys_permission (id, parent_id, name, code, type, path, sort, status, create_time, update_time) VALUES
(71, 7, '应收台账', 'finance:receivable:list', 1, '/finance/receivables', 1, 1, NOW(), NOW()),
(72, 7, '应付台账', 'finance:payable:list',    1, '/finance/payables',    2, 1, NOW(), NOW()),
(73, 7, '收付款',   'finance:payment:list',    1, '/finance/payments',    3, 1, NOW(), NOW());

-- 财务管理按钮
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(731, 73, '确认收付款', 'finance:payment:confirm', 2, 1, 1, NOW(), NOW());

-- ==================== 管理员角色拥有全部权限 ====================
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 1000, 1, id
FROM t_sys_permission WHERE status = 1;

-- ==================== 数据字典初始化 ====================
INSERT INTO t_sys_dict (id, name, code, status, create_time, update_time) VALUES
(1, '物料类型',     'material_category', 1, NOW(), NOW()),
(2, '单据状态',     'order_status',      1, NOW(), NOW()),
(3, '库存变动类型', 'inv_transaction_type',1, NOW(), NOW());

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
(301, 3, '采购入库', 'PURCHASE_IN',   1, 1, NOW(), NOW()),
(302, 3, '生产领料', 'PICKING_OUT',   2, 1, NOW(), NOW()),
(303, 3, '生产退料', 'PICKING_RETURN',3, 1, NOW(), NOW()),
(304, 3, '完工入库', 'FINISH_IN',     4, 1, NOW(), NOW()),
(305, 3, '销售出库', 'SALES_OUT',     5, 1, NOW(), NOW()),
(306, 3, '调拨',     'TRANSFER',      6, 1, NOW(), NOW()),
(307, 3, '盘点调整', 'ADJUST',        7, 1, NOW(), NOW()),
(308, 3, '报废',     'SCRAP',         8, 1, NOW(), NOW());

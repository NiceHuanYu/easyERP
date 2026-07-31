-- 库存导出权限
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(611, 61, '导出库存', 'inventory:stock:export', 2, 4, 1, NOW(), NOW()),
(621, 62, '导出流水', 'inventory:stock:export', 2, 4, 1, NOW(), NOW());

-- 管理员获得权限
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 50000, 1, id
FROM t_sys_permission WHERE code = 'inventory:stock:export';

-- 仓库角色获得导出权限
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 51000, 4, id
FROM t_sys_permission WHERE code = 'inventory:stock:export';

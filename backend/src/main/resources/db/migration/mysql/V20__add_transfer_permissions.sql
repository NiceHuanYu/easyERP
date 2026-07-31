-- 库存调拨菜单权限
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(63, 6, '库存调拨', 'inventory:stock:view', 1, 3, 1, NOW(), NOW());

-- 仓库角色获得调拨权限（与库存查询、库存流水一致）
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 52000, 4, id
FROM t_sys_permission WHERE id = 63;

-- 管理员获得调拨权限
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 52010, 1, id
FROM t_sys_permission WHERE id = 63;

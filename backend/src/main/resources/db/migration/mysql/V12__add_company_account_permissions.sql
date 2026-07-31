-- 公司账户管理权限
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(27, 2, '公司账户', 'base-data:company-account:view', 1, 7, 1, NOW(), NOW());

INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(271, 27, '新增公司账户', 'base-data:company-account:create', 2, 1, 1, NOW(), NOW()),
(272, 27, '编辑公司账户', 'base-data:company-account:edit',   2, 2, 1, NOW(), NOW()),
(273, 27, '删除公司账户', 'base-data:company-account:delete', 2, 3, 1, NOW(), NOW());

-- 管理员获得所有新权限
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 20000, 1, id
FROM t_sys_permission
WHERE code IN ('base-data:company-account:view','base-data:company-account:create','base-data:company-account:edit','base-data:company-account:delete');

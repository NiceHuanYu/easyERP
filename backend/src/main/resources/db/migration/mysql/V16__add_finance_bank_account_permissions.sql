-- 银行账户权限迁移至财务管理模块（finance:bank-account:*）
-- 新增 finance 权限码
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(28, 7, '银行账户', 'finance:bank-account:view', 1, 4, 1, NOW(), NOW());

INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(281, 28, '新增银行账户', 'finance:bank-account:create', 2, 1, 1, NOW(), NOW()),
(282, 28, '编辑银行账户', 'finance:bank-account:edit',   2, 2, 1, NOW(), NOW()),
(283, 28, '删除银行账户', 'finance:bank-account:delete', 2, 3, 1, NOW(), NOW());

-- 管理员获得 finance 权限
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 40000, 1, id
FROM t_sys_permission
WHERE code IN ('finance:bank-account:view','finance:bank-account:create','finance:bank-account:edit','finance:bank-account:delete');

-- 将已有的 base-data:company-account:* 权限也赋给 finance（兼容旧数据）
-- 已有的 role_permission 保持不动，两边都能用

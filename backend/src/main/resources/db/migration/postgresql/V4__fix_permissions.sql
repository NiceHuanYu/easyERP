-- =====================================================
-- V4__fix_permissions.sql (PostgreSQL)
-- 补充缺失权限码 + 角色分配修正
-- =====================================================

-- 1. 新增缺失的按钮权限
INSERT INTO t_sys_permission (id, parent_id, name, code, type, sort, status, create_time, update_time) VALUES
(324, 32, '确认发货',    'delivery:order:approve',       2, 4, 1, NOW(), NOW()),
(421, 42, '确认领料',    'production:picking:confirm',    2, 1, 1, NOW(), NOW()),
(431, 43, '确认完工入库','production:finish:confirm',     2, 1, 1, NOW(), NOW());

-- 2. 管理员自动获得所有新权限
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 10000, 1, id
FROM t_sys_permission
WHERE code IN ('delivery:order:approve', 'production:picking:confirm', 'production:finish:confirm');

-- 3. 仓库角色（role_id=4）：补充 delivery:order:approve
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 11000, 4, id
FROM t_sys_permission WHERE code = 'delivery:order:approve';

-- 4. 生产角色（role_id=5）：补充 picking:confirm 和 finish:confirm
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 12000, 5, id
FROM t_sys_permission
WHERE code IN ('production:picking:confirm', 'production:finish:confirm');

-- 5. 销售角色（role_id=2）：补充 delivery:order:approve
INSERT INTO t_sys_role_permission (id, role_id, permission_id)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 13000, 2, id
FROM t_sys_permission WHERE code = 'delivery:order:approve';

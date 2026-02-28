-- ============================================================
-- RBAC 权限系统初始化测试数据
-- 注意：密码均使用 BCrypt 加密
--   admin123  → $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaIWlHQVuFMWm
--   oper123   → $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW
--   audit123  → $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- ============================================================

-- ============================================================
-- 清理（按外键顺序）
-- ============================================================
DELETE FROM role_permissions;
DELETE FROM user_roles;
DELETE FROM permissions;
DELETE FROM roles;
DELETE FROM users;

-- ============================================================
-- 1. 插入权限（覆盖 user / report / config 三个模块）
-- ============================================================
INSERT INTO permissions (id, permission_code, permission_name, module, description)
VALUES
    -- 用户模块
    (1,  'user:view',    '查看用户',   'user',   '可查看用户列表和详情'),
    (2,  'user:create',  '创建用户',   'user',   '可新增用户'),
    (3,  'user:update',  '修改用户',   'user',   '可编辑用户信息'),
    (4,  'user:delete',  '删除用户',   'user',   '可删除用户（危险操作）'),
    -- 报表模块
    (5,  'report:view',  '查看报表',   'report', '可查看数据报表'),
    (6,  'report:export','导出报表',   'report', '可导出报表文件（report:export）'),
    -- 配置模块
    (7,  'config:view',  '查看配置',   'config', '可查看系统配置'),
    (8,  'config:update','修改配置',   'config', '可修改系统参数（config:update）'),
    -- 角色模块
    (9,  'role:view',    '查看角色',   'role',   '可查看角色列表'),
    (10, 'role:manage',  '管理角色',   'role',   '可新增/编辑/删除角色及其权限');

-- 重置序列
SELECT setval('permissions_id_seq', 10, true);

-- ============================================================
-- 2. 插入角色
-- ============================================================
INSERT INTO roles (id, role_code, role_name, description)
VALUES
    (1, 'ADMIN',    '超级管理员', '拥有系统全部权限'),
    (2, 'OPERATOR', '业务操作员', '可操作用户数据和导出报表'),
    (3, 'AUDITOR',  '审计人员',   '只读权限，可查看用户/报表/配置');

SELECT setval('roles_id_seq', 3, true);

-- ============================================================
-- 3. 角色 → 权限 关联
-- ============================================================
-- ADMIN：全部权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- OPERATOR：user:view/create/update + report:view/export
INSERT INTO role_permissions (role_id, permission_id) VALUES
    (2, 1),  -- user:view
    (2, 2),  -- user:create
    (2, 3),  -- user:update
    (2, 5),  -- report:view
    (2, 6);  -- report:export

-- AUDITOR：所有 :view 权限（只读）
INSERT INTO role_permissions (role_id, permission_id) VALUES
    (3, 1),  -- user:view
    (3, 5),  -- report:view
    (3, 7);  -- config:view

-- ============================================================
-- 4. 插入测试用户
-- ============================================================
INSERT INTO users (id, username, email, password, status)
VALUES
    (1, 'admin',    'admin@example.com',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaIWlHQVuFMWm', 1),
    (2, 'operator', 'operator@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 1),
    (3, 'auditor',  'auditor@example.com',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',  1);

SELECT setval('users_id_seq', 3, true);

-- ============================================================
-- 5. 用户 → 角色 关联
-- ============================================================
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1),  -- admin    → ADMIN
    (2, 2),  -- operator → OPERATOR
    (3, 3);  -- auditor  → AUDITOR

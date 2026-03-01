    -- ============================================================
    -- RBAC 权限系统建表脚本
    -- 数据库: PostgreSQL
    -- 编码: UTF-8
    -- ============================================================

    -- ============================================================
    -- 1. 用户表 (password 字段改为 BCrypt 加密，长度 60)
    -- ============================================================
    CREATE TABLE IF NOT EXISTS users (
        id          BIGSERIAL    PRIMARY KEY,
        username    VARCHAR(20)  NOT NULL UNIQUE,
        email       VARCHAR(100) NOT NULL UNIQUE,
        password    VARCHAR(60)  NOT NULL,
        status      SMALLINT     NOT NULL DEFAULT 1,   -- 1:启用 0:禁用
        create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
        update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
        is_deleted  INT          NOT NULL DEFAULT 0
    );

    CREATE INDEX IF NOT EXISTS idx_users_username  ON users(username);
    CREATE INDEX IF NOT EXISTS idx_users_email     ON users(email);

    COMMENT ON TABLE  users              IS '用户表';
    COMMENT ON COLUMN users.id           IS '主键ID';
    COMMENT ON COLUMN users.username     IS '用户名（唯一）';
    COMMENT ON COLUMN users.email        IS '邮箱（唯一）';
    COMMENT ON COLUMN users.password     IS '密码（BCrypt 加密，60位）';
    COMMENT ON COLUMN users.status       IS '账号状态：1-启用，0-禁用';
    COMMENT ON COLUMN users.create_time  IS '创建时间';
    COMMENT ON COLUMN users.update_time  IS '更新时间';
    COMMENT ON COLUMN users.is_deleted   IS '逻辑删除：0-未删除，1-已删除';

    -- ============================================================
    -- 2. 角色表
    -- ============================================================
    CREATE TABLE IF NOT EXISTS roles (
        id          BIGSERIAL    PRIMARY KEY,
        role_code   VARCHAR(50)  NOT NULL UNIQUE,   -- 如 ADMIN / OPERATOR / AUDITOR
        role_name   VARCHAR(100) NOT NULL,
        description VARCHAR(255),
        create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
        update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
        is_deleted  INT          NOT NULL DEFAULT 0
    );

    CREATE INDEX IF NOT EXISTS idx_roles_code ON roles(role_code);

    COMMENT ON TABLE  roles              IS '角色表';
    COMMENT ON COLUMN roles.id           IS '主键ID';
    COMMENT ON COLUMN roles.role_code    IS '角色编码（唯一），如 ADMIN';
    COMMENT ON COLUMN roles.role_name    IS '角色名称';
    COMMENT ON COLUMN roles.description  IS '角色描述';
    COMMENT ON COLUMN roles.create_time  IS '创建时间';
    COMMENT ON COLUMN roles.update_time  IS '更新时间';
    COMMENT ON COLUMN roles.is_deleted   IS '逻辑删除：0-未删除，1-已删除';

    -- ============================================================
    -- 3. 权限表
    -- ============================================================
    CREATE TABLE IF NOT EXISTS permissions (
        id              BIGSERIAL    PRIMARY KEY,
        permission_code VARCHAR(100) NOT NULL UNIQUE,  -- 如 user:delete / report:export
        permission_name VARCHAR(100) NOT NULL,
        module          VARCHAR(50)  NOT NULL,          -- 所属模块，如 user / report / config
        description     VARCHAR(255),
        create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
        update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
        is_deleted      INT          NOT NULL DEFAULT 0
    );

    CREATE INDEX IF NOT EXISTS idx_permissions_code   ON permissions(permission_code);
    CREATE INDEX IF NOT EXISTS idx_permissions_module ON permissions(module);

    COMMENT ON TABLE  permissions                  IS '权限表';
    COMMENT ON COLUMN permissions.id               IS '主键ID';
    COMMENT ON COLUMN permissions.permission_code  IS '权限编码（唯一），如 user:delete';
    COMMENT ON COLUMN permissions.permission_name  IS '权限名称';
    COMMENT ON COLUMN permissions.module           IS '所属业务模块';
    COMMENT ON COLUMN permissions.description      IS '权限描述';
    COMMENT ON COLUMN permissions.create_time      IS '创建时间';
    COMMENT ON COLUMN permissions.update_time      IS '更新时间';
    COMMENT ON COLUMN permissions.is_deleted       IS '逻辑删除：0-未删除，1-已删除';

    -- ============================================================
    -- 4. 用户-角色 中间表（多对多）
    -- ============================================================
    CREATE TABLE IF NOT EXISTS user_roles (
        id          BIGSERIAL PRIMARY KEY,
        user_id     BIGINT    NOT NULL,
        role_id     BIGINT    NOT NULL,
        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT uq_user_role UNIQUE (user_id, role_id),
        CONSTRAINT fk_ur_user   FOREIGN KEY (user_id) REFERENCES users(id),
        CONSTRAINT fk_ur_role   FOREIGN KEY (role_id) REFERENCES roles(id)
    );

    CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
    CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

    COMMENT ON TABLE  user_roles             IS '用户-角色关联表（多对多）';
    COMMENT ON COLUMN user_roles.user_id     IS '用户ID';
    COMMENT ON COLUMN user_roles.role_id     IS '角色ID';
    COMMENT ON COLUMN user_roles.create_time IS '关联创建时间';

    -- ============================================================
    -- 5. 角色-权限 中间表（多对多）
    -- ============================================================
    CREATE TABLE IF NOT EXISTS role_permissions (
        id            BIGSERIAL PRIMARY KEY,
        role_id       BIGINT    NOT NULL,
        permission_id BIGINT    NOT NULL,
        create_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id),
        CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)       REFERENCES roles(id),
        CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
    );

    CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id       ON role_permissions(role_id);
    CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions(permission_id);

    COMMENT ON TABLE  role_permissions               IS '角色-权限关联表（多对多）';
    COMMENT ON COLUMN role_permissions.role_id       IS '角色ID';
    COMMENT ON COLUMN role_permissions.permission_id IS '权限ID';
    COMMENT ON COLUMN role_permissions.create_time   IS '关联创建时间';

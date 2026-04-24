package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // 兼容已有数据库：补齐会员密码字段，避免会员管理查询/登录时报错
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'member'
                  AND COLUMN_NAME = 'password'
                """,
                Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE member ADD COLUMN password VARCHAR(128) NOT NULL DEFAULT '' AFTER phone");
        }

        // 兼容已有数据库：补齐商品分类表结构，避免新增/编辑分类时报错
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS category (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(64) NOT NULL,
                    status TINYINT NOT NULL DEFAULT 1,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

        Integer statusCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'category'
                  AND COLUMN_NAME = 'status'
                """,
                Integer.class);
        if (statusCount != null && statusCount == 0) {
            jdbcTemplate.execute("ALTER TABLE category ADD COLUMN status TINYINT NOT NULL DEFAULT 1 AFTER name");
        }

        Integer sortCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'category'
                  AND COLUMN_NAME = 'sort'
                """,
                Integer.class);
        if (sortCount != null && sortCount > 0) {
            jdbcTemplate.execute("ALTER TABLE category DROP COLUMN sort");
        }

        Integer parentIdCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'category'
                  AND COLUMN_NAME = 'parent_id'
                """,
                Integer.class);
        if (parentIdCount != null && parentIdCount > 0) {
            jdbcTemplate.execute("ALTER TABLE category DROP COLUMN parent_id");
        }

        Integer createdAtCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'category'
                  AND COLUMN_NAME = 'created_at'
                """,
                Integer.class);
        if (createdAtCount != null && createdAtCount == 0) {
            jdbcTemplate.execute("ALTER TABLE category ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        }

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS member_balance_log (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    member_id BIGINT NOT NULL,
                    delta_amount DECIMAL(10,2) NOT NULL,
                    after_balance DECIMAL(10,2) NOT NULL,
                    biz_type VARCHAR(32) NOT NULL,
                    remark VARCHAR(255) NULL,
                    operator_name VARCHAR(64) NOT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_member_balance_log_member(member_id),
                    INDEX idx_member_balance_log_created(created_at)
                )
                """);

        // 兼容已有数据库：补齐商品分类权限点，并授予 ADMIN / STAFF
        jdbcTemplate.update(
                "INSERT IGNORE INTO sys_permission(code, name, type) VALUES (?, ?, ?)",
                "product:category:create", "创建商品分类", "API");
        jdbcTemplate.update(
                "INSERT IGNORE INTO sys_permission(code, name, type) VALUES (?, ?, ?)",
                "product:category:update", "更新商品分类", "API");
        jdbcTemplate.update(
                "INSERT IGNORE INTO sys_permission(code, name, type) VALUES (?, ?, ?)",
                "product:category:delete", "删除商品分类", "API");

        jdbcTemplate.execute("""
                INSERT IGNORE INTO sys_role_permission(role_id, permission_id)
                SELECT r.id, p.id
                FROM sys_role r
                JOIN sys_permission p ON p.code IN (
                    'product:category:create',
                    'product:category:update',
                    'product:category:delete'
                )
                WHERE r.code IN ('ADMIN', 'STAFF')
                """);

        // 兼容已有数据库：按 sys_user.role 回填 sys_user_role 关系，避免老账号拿不到权限
        jdbcTemplate.execute("""
                INSERT IGNORE INTO sys_user_role(user_id, role_id)
                SELECT u.id, r.id
                FROM sys_user u
                JOIN sys_role r ON r.code = u.role
                """);
    }
}

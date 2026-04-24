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
    }
}

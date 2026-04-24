CREATE DATABASE IF NOT EXISTS supermarket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE supermarket;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'STAFF',
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(16) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    name VARCHAR(32) NOT NULL,
    gender TINYINT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    points INT NOT NULL DEFAULT 0,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 兼容低版本 MySQL：不使用 ADD COLUMN IF NOT EXISTS
-- 若是升级已有库，请手工执行一次：
-- ALTER TABLE member ADD COLUMN password VARCHAR(128) NOT NULL DEFAULT '' AFTER phone;

CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    barcode VARCHAR(64) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL,
    purchase_price DECIMAL(10,2) NOT NULL,
    sale_price DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_name(name),
    INDEX idx_product_category(category_id)
);

CREATE TABLE IF NOT EXISTS inventory (
    product_id BIGINT PRIMARY KEY,
    quantity INT NOT NULL DEFAULT 0,
    warning_qty INT NOT NULL DEFAULT 10,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    delta_qty INT NOT NULL,
    after_qty INT NOT NULL,
    biz_type VARCHAR(32) NOT NULL,
    remark VARCHAR(255) NULL,
    operator_name VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_inventory_log_product(product_id),
    INDEX idx_inventory_log_created(created_at)
);

CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    contact_name VARCHAR(64) NULL,
    contact_phone VARCHAR(32) NULL,
    address VARCHAR(255) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_by VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    audited_at DATETIME NULL,
    INDEX idx_purchase_order_supplier(supplier_id),
    INDEX idx_purchase_order_created(created_at)
);

CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    INDEX idx_purchase_item_order(order_id),
    INDEX idx_purchase_item_product(product_id)
);

CREATE TABLE IF NOT EXISTS sales_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    member_id BIGINT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status TINYINT NOT NULL DEFAULT 1,
    pay_type VARCHAR(32) NOT NULL,
    cashier_name VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sales_order_created(created_at)
);

CREATE TABLE IF NOT EXISTS sales_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    INDEX idx_sales_item_order(order_id),
    INDEX idx_sales_item_product(product_id)
);

CREATE TABLE IF NOT EXISTS sales_refund (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sales_order_id BIGINT NOT NULL,
    refund_no VARCHAR(64) NOT NULL UNIQUE,
    refund_amount DECIMAL(12,2) NOT NULL,
    operator_name VARCHAR(64) NOT NULL,
    reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sales_refund_order(sales_order_id),
    INDEX idx_sales_refund_created(created_at)
);

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
);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY(role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY(user_id, role_id)
);

INSERT IGNORE INTO sys_role(code, name, status) VALUES ('ADMIN', '管理员', 1), ('STAFF', '员工', 1);

INSERT IGNORE INTO sys_permission(code, name, type) VALUES
('product:category:create', '创建商品分类', 'API'),
('product:category:update', '更新商品分类', 'API'),
('product:category:delete', '删除商品分类', 'API'),
('product:create', '创建商品', 'API'),
('product:update', '更新商品', 'API'),
('product:status:update', '商品上下架', 'API'),
('product:delete', '删除商品', 'API'),
('inventory:adjust', '库存调整', 'API'),
('inventory:warning:update', '更新库存预警值', 'API'),
('purchase:supplier:create', '创建供应商', 'API'),
('purchase:supplier:update', '更新供应商', 'API'),
('purchase:supplier:status:update', '更新供应商状态', 'API'),
('purchase:order:create', '创建采购单', 'API'),
('purchase:order:stock-in', '采购单入库', 'API'),
('purchase:order:cancel', '取消采购单', 'API'),
('sales:create', '销售开单', 'API'),
('sales:refund', '销售退款', 'API');

INSERT IGNORE INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.code = 'ADMIN';

INSERT IGNORE INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM sys_role r
JOIN sys_permission p ON p.code IN (
    'product:create', 'product:update', 'product:status:update',
    'inventory:adjust', 'inventory:warning:update',
    'purchase:supplier:create', 'purchase:supplier:update', 'purchase:supplier:status:update',
    'purchase:order:create', 'purchase:order:stock-in', 'purchase:order:cancel',
    'sales:create', 'sales:refund'
)
WHERE r.code = 'STAFF';

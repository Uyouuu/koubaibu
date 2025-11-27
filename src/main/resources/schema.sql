-- 研究室購買部在庫管理システム - データベーススキーマ
-- Laboratory Purchasing Department Inventory Management System - Database Schema

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_price_positive CHECK (price >= 0),
    CONSTRAINT chk_stock_non_negative CHECK (stock_quantity >= 0)
);

-- Alert settings table
CREATE TABLE IF NOT EXISTS alert_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    threshold INT NOT NULL DEFAULT 2,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_threshold_range CHECK (threshold >= 0 AND threshold <= 999)
);

-- Alert history table
CREATE TABLE IF NOT EXISTS alert_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    alert_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    message VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SENT',
    operator_name VARCHAR(64),
    request_id VARCHAR(64),
    CONSTRAINT fk_alert_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Inventory events table (audit log)
CREATE TABLE IF NOT EXISTS inventory_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    operator_name VARCHAR(64) NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    delta INT,
    snapshot_stock INT NOT NULL,
    request_id VARCHAR(64),
    metadata CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_alert_history_alert_time ON alert_history(alert_time);
CREATE INDEX IF NOT EXISTS idx_inventory_events_created_at ON inventory_events(created_at);
CREATE INDEX IF NOT EXISTS idx_inventory_events_operator_name ON inventory_events(operator_name);

-- Insert default alert settings
INSERT INTO alert_settings (threshold) SELECT 2 WHERE NOT EXISTS (SELECT 1 FROM alert_settings);

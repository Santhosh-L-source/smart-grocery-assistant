-- V1__init_schema.sql
-- Smart Grocery Assistant initial relational schema

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    full_name VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_en VARCHAR(150) NOT NULL,
    name_ta VARCHAR(150),
    barcode VARCHAR(100) UNIQUE,
    category VARCHAR(100) NOT NULL,
    product_type VARCHAR(50) NOT NULL DEFAULT 'PACKAGED',
    unit VARCHAR(30) NOT NULL,
    purchase_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    selling_price DECIMAL(10, 2) NOT NULL,
    stock_quantity DECIMAL(10, 3) NOT NULL DEFAULT 0.000,
    low_stock_threshold DECIMAL(10, 3) NOT NULL DEFAULT 5.000,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_products_name_en (name_en),
    INDEX idx_products_name_ta (name_ta),
    INDEX idx_products_barcode (barcode)
);

CREATE TABLE IF NOT EXISTS price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    old_selling_price DECIMAL(10, 2) NOT NULL,
    new_selling_price DECIMAL(10, 2) NOT NULL,
    old_purchase_price DECIMAL(10, 2),
    new_purchase_price DECIMAL(10, 2),
    update_method VARCHAR(20) NOT NULL,
    admin_username VARCHAR(50) NOT NULL,
    change_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_hist_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_price_hist_product_id (product_id),
    INDEX idx_price_hist_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_number VARCHAR(64) NOT NULL UNIQUE,
    subtotal DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_mode VARCHAR(30) NOT NULL DEFAULT 'CASH',
    cashier_username VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sales_created_at (created_at),
    INDEX idx_sales_number (sale_number)
);

CREATE TABLE IF NOT EXISTS sale_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name_en VARCHAR(150) NOT NULL,
    product_name_ta VARCHAR(150),
    unit_code VARCHAR(30) NOT NULL,
    unit_price_snapshot DECIMAL(10, 2) NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_sale_items_sale (sale_id),
    INDEX idx_sale_items_product (product_id)
);

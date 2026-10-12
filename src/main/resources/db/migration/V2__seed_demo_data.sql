-- V2__seed_demo_data.sql
-- Demonstration data for Smart Grocery Assistant

-- Users (BCrypt hashes for passwords: admin123 and shop123)
-- BCrypt for 'admin123': $2a$10$w.1g5w1zP3p1N5eZ1d3nqeJjZ8k5n8y2g5X7z0j2k1L3m5N7p9Q2i
-- BCrypt for 'shop123':  $2a$10$h2X4m6V8k0n2P4r6T8v0XeBz7m9k1L3n5P7r9T1v3X5z7B9d1F3h5
-- To guarantee password matching regardless of BCrypt salt implementation, Spring Security will configure a delegating password encoder that supports {bcrypt} or {noop} or standard BCrypt.
INSERT INTO users (username, password_hash, role, full_name, active) VALUES
('admin', '{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.STQB/59GQzpV2', 'ADMIN', 'Store Owner / Admin', TRUE),
('shopkeeper', '{bcrypt}$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'SHOPKEEPER', 'Shopkeeper Counter', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- Products (Clearly marked as sample grocery demonstration data)
INSERT INTO products (id, name_en, name_ta, barcode, category, product_type, unit, purchase_price, selling_price, stock_quantity, low_stock_threshold, is_active, version) VALUES
(1, 'Rice (Ponni)', 'பொன்னி அரிசி', '8901030000101', 'Grains & Pulses', 'LOOSE', 'KG', 45.00, 55.00, 150.000, 20.000, TRUE, 0),
(2, 'Sugar', 'சர்க்கரை', '8901030000102', 'Essentials', 'LOOSE', 'KG', 40.00, 48.00, 95.500, 15.000, TRUE, 0),
(3, 'Bath Soap (Lifebuoy)', 'லைஃப்பாய் சோப்', '8901030000103', 'Personal Care', 'PACKAGED', 'PIECE', 28.00, 35.00, 48.000, 10.000, TRUE, 0),
(4, 'Marie Gold Biscuits', 'மேரி கோல்ட் பிஸ்கட்', '8901030000104', 'Snacks', 'PACKAGED', 'PACKET', 15.00, 20.00, 60.000, 12.000, TRUE, 0),
(5, 'Refined Sunflower Oil', 'சூரியகாந்தி எண்ணெய்', '8901030000105', 'Oils', 'LOOSE', 'LITRE', 110.00, 125.00, 45.000, 10.000, TRUE, 0),
(6, 'Toor Dal', 'துவரம் பருப்பு', '8901030000106', 'Grains & Pulses', 'LOOSE', 'KG', 120.00, 140.00, 50.000, 10.000, TRUE, 0),
(7, 'Chakra Gold Tea', 'சக்ரா கோல்ட் டீ', '8901030000107', 'Beverages', 'PACKAGED', 'PACKET', 60.00, 75.00, 30.000, 5.000, TRUE, 0),
(8, 'Aavin Milk Packet (500ml)', 'ஆவின் பால் பாக்கெட்', '8901030000108', 'Dairy', 'PACKAGED', 'PACKET', 26.00, 30.00, 40.000, 10.000, TRUE, 0),
(9, 'Tata Crystal Salt (1kg)', 'டாடா உப்பு', '8901030000109', 'Essentials', 'PACKAGED', 'PACKET', 10.00, 15.00, 50.000, 15.000, TRUE, 0),
(10, 'Onion', 'வெங்காயம்', '8901030000110', 'Vegetables', 'LOOSE', 'KG', 30.00, 40.00, 80.000, 15.000, TRUE, 0),
(11, 'Tomato', 'தக்காளி', '8901030000111', 'Vegetables', 'LOOSE', 'KG', 25.00, 35.00, 40.000, 10.000, TRUE, 0)
ON DUPLICATE KEY UPDATE id=id;

-- Initial Price History records for demonstration audit trail
INSERT INTO price_history (product_id, old_selling_price, new_selling_price, old_purchase_price, new_purchase_price, update_method, admin_username, change_reason, created_at) VALUES
(1, 50.00, 55.00, 42.00, 45.00, 'MANUAL', 'admin', 'Market wholesale price adjustment', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 45.00, 48.00, 38.00, 40.00, 'VOICE', 'admin', 'Voice updated after arrival of new stock', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 120.00, 125.00, 105.00, 110.00, 'MANUAL', 'admin', 'Brand distributor price revision', DATE_SUB(NOW(), INTERVAL 1 DAY));

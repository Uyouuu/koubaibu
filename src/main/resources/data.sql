-- Sample data for development environment

-- Sample products (using MERGE to avoid duplicate key violations)
MERGE INTO products (id, name, price, stock_quantity, version) KEY(id) VALUES 
(1, 'ボールペン (黒)', 120.00, 25, 0),
(2, 'ボールペン (赤)', 120.00, 15, 0),
(3, 'ボールペン (青)', 120.00, 20, 0),
(4, 'シャープペンシル', 250.00, 10, 0),
(5, '消しゴム', 80.00, 30, 0),
(6, 'ノート (A4)', 180.00, 12, 0),
(7, 'ノート (B5)', 150.00, 18, 0),
(8, '付箋紙', 200.00, 8, 0),
(9, '修正テープ', 220.00, 5, 0),
(10, 'ホッチキス', 350.00, 3, 0);

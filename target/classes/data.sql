-- Default users (password: password123 -> BCrypt hash)
-- Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWq
INSERT INTO users (first_name, last_name, email, password, role, phone, active)
VALUES ('Admin', 'User', 'admin@kota.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWq', 'ADMIN', '0000000000', TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (first_name, last_name, email, password, role, phone, active)
VALUES ('Staff', 'Member', 'staff@kota.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWq', 'STAFF', '0000000001', TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (first_name, last_name, email, password, role, phone, active)
VALUES ('Student', 'User', 'student@kota.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWq', 'STUDENT', '0000000002', TRUE)
ON CONFLICT (email) DO NOTHING;

-- Ingredients
INSERT INTO ingredients (name, unit, current_stock, reorder_point, reorder_quantity, cost_per_unit, active)
VALUES
  ('Bread Loaf Quarter', 'piece', 100, 20, 50, 2.50, TRUE),
  ('Polony Slice', 'slice', 200, 30, 100, 1.00, TRUE),
  ('Cheese Slice', 'slice', 150, 25, 80, 1.50, TRUE),
  ('Sausage', 'piece', 100, 20, 50, 3.00, TRUE),
  ('French Fries', 'portion', 80, 15, 40, 4.00, TRUE),
  ('Egg', 'piece', 100, 20, 50, 1.20, TRUE),
  ('Chicken Strip', 'piece', 80, 15, 40, 5.00, TRUE)
ON CONFLICT DO NOTHING;

-- Menu items
INSERT INTO menu_items (name, description, price, available, preparation_minutes)
VALUES
  ('Full Kota', 'Quarter bread loaded with polony, cheese, sausage and chips', 35.00, TRUE, 10),
  ('Mini Kota', 'Quarter bread with polony and chips', 25.00, TRUE, 8),
  ('Chips Only', 'Fresh fried chips', 15.00, TRUE, 5),
  ('Chicken Kota', 'Quarter bread with chicken strips and chips', 40.00, TRUE, 12),
  ('Cool Drink', 'Chilled 330ml can', 10.00, TRUE, 1)
ON CONFLICT DO NOTHING;

-- Recipes (menu_item_id, ingredient_id, quantity) - referenced by position inserts so using subqueries
INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 1
FROM menu_items m, ingredients i
WHERE m.name = 'Full Kota' AND i.name = 'Bread Loaf Quarter'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 2
FROM menu_items m, ingredients i
WHERE m.name = 'Full Kota' AND i.name = 'Polony Slice'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 1
FROM menu_items m, ingredients i
WHERE m.name = 'Full Kota' AND i.name = 'Cheese Slice'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 1
FROM menu_items m, ingredients i
WHERE m.name = 'Full Kota' AND i.name = 'Sausage'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 1
FROM menu_items m, ingredients i
WHERE m.name = 'Full Kota' AND i.name = 'French Fries'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 1
FROM menu_items m, ingredients i
WHERE m.name = 'Mini Kota' AND i.name = 'Bread Loaf Quarter'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 1
FROM menu_items m, ingredients i
WHERE m.name = 'Mini Kota' AND i.name = 'Polony Slice'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 1
FROM menu_items m, ingredients i
WHERE m.name = 'Mini Kota' AND i.name = 'French Fries'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 1
FROM menu_items m, ingredients i
WHERE m.name = 'Chips Only' AND i.name = 'French Fries'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 1
FROM menu_items m, ingredients i
WHERE m.name = 'Chicken Kota' AND i.name = 'Bread Loaf Quarter'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 2
FROM menu_items m, ingredients i
WHERE m.name = 'Chicken Kota' AND i.name = 'Chicken Strip'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

INSERT INTO recipes (menu_item_id, ingredient_id, quantity)
SELECT m.id, i.id, 1
FROM menu_items m, ingredients i
WHERE m.name = 'Chicken Kota' AND i.name = 'French Fries'
ON CONFLICT (menu_item_id, ingredient_id) DO NOTHING;

-- System settings
INSERT INTO system_settings (setting_key, setting_value, description)
VALUES
  ('operational.open_hour', '10', 'Hour when ordering opens (24h format)'),
  ('operational.close_hour', '22', 'Hour when ordering closes (24h format)'),
  ('order.throttle_window_minutes', '10', 'Time window for order throttling'),
  ('order.throttle_max_orders', '30', 'Max orders per throttle window'),
  ('order.max_pending_orders', '20', 'Max orders allowed in queue'),
  ('order.auto_cancel_minutes', '30', 'Minutes before ready order is auto-cancelled')
ON CONFLICT (setting_key) DO NOTHING;

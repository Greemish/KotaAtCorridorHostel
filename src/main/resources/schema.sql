CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS menu_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    preparation_minutes INTEGER NOT NULL DEFAULT 10,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ingredients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    current_stock DECIMAL(10,3) NOT NULL DEFAULT 0,
    reorder_point DECIMAL(10,3) NOT NULL DEFAULT 0,
    reorder_quantity DECIMAL(10,3) NOT NULL DEFAULT 0,
    cost_per_unit DECIMAL(10,4) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recipes (
    id BIGSERIAL PRIMARY KEY,
    menu_item_id BIGINT NOT NULL REFERENCES menu_items(id),
    ingredient_id BIGINT NOT NULL REFERENCES ingredients(id),
    quantity DECIMAL(10,3) NOT NULL,
    UNIQUE (menu_item_id, ingredient_id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(30) NOT NULL UNIQUE,
    student_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    total_amount DECIMAL(10,2) NOT NULL,
    cogs_amount DECIMAL(10,2),
    notes VARCHAR(500),
    queue_position INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    completed_at TIMESTAMP,
    ready_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    menu_item_id BIGINT NOT NULL REFERENCES menu_items(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    cogs_per_item DECIMAL(10,4)
);

CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    yoco_checkout_id VARCHAR(255),
    yoco_payment_id VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    yoco_fee DECIMAL(10,2),
    currency VARCHAR(10) NOT NULL DEFAULT 'ZAR',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    card_last4 VARCHAR(4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_transaction_id BIGINT NOT NULL REFERENCES payment_transactions(id),
    yoco_refund_id VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS stock_transactions (
    id BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL REFERENCES ingredients(id),
    transaction_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    previous_stock DECIMAL(10,3),
    new_stock DECIMAL(10,3),
    reason VARCHAR(500),
    order_id BIGINT REFERENCES orders(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    message VARCHAR(500) NOT NULL,
    type VARCHAR(50),
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    related_order_id BIGINT
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(200) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    reason VARCHAR(500),
    ip_address VARCHAR(50),
    performed_by_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS system_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(500),
    description VARCHAR(500),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS yoco_settings (
    id BIGSERIAL PRIMARY KEY,
    secret_key VARCHAR(500),
    webhook_secret VARCHAR(500),
    environment VARCHAR(20) NOT NULL DEFAULT 'test',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS webhook_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100),
    payload TEXT,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orders_student_id ON orders(student_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_transactions_order_id ON payment_transactions(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_transactions_yoco_checkout_id ON payment_transactions(yoco_checkout_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_stock_transactions_ingredient_id ON stock_transactions(ingredient_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_webhook_events_event_id ON webhook_events(event_id);

CREATE TABLE orders (
    id              UUID PRIMARY KEY,
    order_number    VARCHAR(50)    NOT NULL UNIQUE,
    customer_name   VARCHAR(100)   NOT NULL,
    customer_email  VARCHAR(255)   NOT NULL,
    total_amount    NUMERIC(19, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL DEFAULT 'IDR',
    status          VARCHAR(30)    NOT NULL DEFAULT 'CREATED',
    payment_id      UUID,
    notes           TEXT,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_order_number ON orders (order_number);
CREATE INDEX idx_order_status ON orders (status);

CREATE TABLE order_items (
    id           UUID PRIMARY KEY,
    order_id     UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_name VARCHAR(255)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   NUMERIC(19, 2) NOT NULL,
    subtotal     NUMERIC(19, 2) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);

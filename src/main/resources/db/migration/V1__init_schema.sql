-- =====================================================
-- SALES SESSIONS
-- =====================================================
-- Local snapshot of the sessions managed by the Catalog service.
-- Stores the event/session information required by Sales to
-- manage ticket availability and the sales window.
-- snapshot_version identifies the latest Catalog version
-- successfully applied to this local snapshot.

CREATE TABLE sales_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    catalog_event_id UUID NOT NULL,
    catalog_session_id UUID NOT NULL,
    catalog_venue_id UUID NOT NULL,
    catalog_event_name VARCHAR(255) NOT NULL,

    catalog_session_start_at TIMESTAMPTZ NOT NULL,
    catalog_sales_start_at TIMESTAMPTZ NOT NULL,
    catalog_sales_end_at TIMESTAMPTZ NOT NULL,
    catalog_max_tickets_per_order INTEGER NOT NULL,
    catalog_venue_name VARCHAR(255) NOT NULL,
    catalog_venue_city VARCHAR(255) NOT NULL,

    snapshot_version BIGINT NOT NULL,

    snapshot_created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    snapshot_updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_sales_sessions_catalog_session
        UNIQUE (catalog_session_id),

    CONSTRAINT chk_sales_sessions_sales_window
        CHECK (catalog_sales_end_at > catalog_sales_start_at),

    CONSTRAINT chk_sales_sessions_max_tickets
        CHECK (catalog_max_tickets_per_order > 0),

    CONSTRAINT chk_sales_sessions_snapshot_version
        CHECK (snapshot_version > 0)
);


-- =====================================================
-- SESSION SEATS
-- =====================================================
-- Represents the commercial availability of each seat
-- for a specific session.
-- Each record refers to a physical seat managed by Catalog,
-- while Sales independently tracks its status and price.
-- catalog_seat_label is the human-readable label of the seat
-- (for example the row and number shown to the customer).
-- Does not store seat map or graphical information.
-- Status meaning:
--   FREE : available for purchase.
--   HELD : locked by an order in PENDING_PAYMENT, until the
--          order expires or is paid.
--   SOLD : purchased (order PAID).

CREATE TABLE session_seats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    sales_session_id UUID NOT NULL,
    catalog_sector_id UUID NOT NULL,
    catalog_seat_id UUID NOT NULL,
    catalog_seat_label VARCHAR(50) NOT NULL,
    catalog_sector_name VARCHAR(50) NOT NULL,

   status VARCHAR(10) NOT NULL DEFAULT 'FREE',
   catalog_price VARCHAR(10) NOT NULL,
   catalog_currency CHAR(3) NOT NULL DEFAULT 'EUR',

    CONSTRAINT fk_session_seats_sales_session
       FOREIGN KEY (sales_session_id)
           REFERENCES sales_sessions(id),

    CONSTRAINT uq_session_seats_session_seat
       UNIQUE (sales_session_id, catalog_seat_id),

    CONSTRAINT chk_session_seats_status
       CHECK (status IN ('FREE', 'HELD', 'SOLD')),

    CONSTRAINT chk_session_seats_price
       CHECK (catalog_price ~ '^\d+(\.\d+)?$' AND catalog_price::NUMERIC >= 0),

    CONSTRAINT chk_session_seats_currency
       CHECK (catalog_currency IN ('EUR', 'USD', 'JPY', 'GBP'))
);


-- =====================================================
-- SALES SESSIONS INDEXES
-- =====================================================

CREATE INDEX idx_sales_sessions_catalog_event
    ON sales_sessions(catalog_event_id);


-- =====================================================
-- SESSION SEATS INDEXES
-- =====================================================

CREATE INDEX idx_session_seats_status
    ON session_seats(sales_session_id, status);



-- =====================================================
-- ORDERS
-- =====================================================
-- Represents a customer's purchase.
-- An order is created at checkout, when the customer confirms
-- the selected seats. In the same transaction the seats are
-- locked (HELD) and the payment timer (expires_at) starts.
-- Seat selection before checkout is not persisted and does
-- not lock any seat.
-- Stores customer and price snapshots, payment information
-- and the current order status.
-- Payment processing is managed by the Payments service.

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    sales_session_id UUID NOT NULL,

    customer_id UUID NOT NULL,

    total_amount VARCHAR(10) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'EUR',

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',

    expires_at TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_sales_session
        FOREIGN KEY (sales_session_id)
            REFERENCES sales_sessions(id),

    CONSTRAINT chk_orders_total_amount
        CHECK (total_amount ~ '^\d+(\.\d+)?$' AND total_amount::NUMERIC >= 0),

    CONSTRAINT chk_currency
        CHECK (
            currency IN ('EUR', 'USD', 'JPY', 'GBP')),

    CONSTRAINT chk_orders_status
        CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'PAYMENT_FAILED', 'EXPIRED', 'CANCELLED'))
);

-- =====================================================
-- ORDER PAYMENTS
-- =====================================================
-- Tracks payment attempts or references associated with an order
-- as managed by the external Payments service.
-- payments_payment_id is unique to prevent duplicate linkage
-- of the same external payment across orders or retries.
CREATE TABLE order_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sales_order_id UUID NOT NULL,
    payments_payment_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_payments_order
        FOREIGN KEY (sales_order_id) REFERENCES orders(id),

    CONSTRAINT uq_order_payments_external
        UNIQUE (payments_payment_id)
);

CREATE INDEX idx_order_payments_order_id ON order_payments(sales_order_id);


-- =====================================================
-- ORDERS INDEXES
-- =====================================================

CREATE INDEX idx_orders_customer
    ON orders(customer_id);

CREATE INDEX idx_orders_session
    ON orders(sales_session_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_payment_expiration
    ON orders(expires_at)
    WHERE status = 'PENDING_PAYMENT';



-- =====================================================
-- ORDER ITEMS
-- =====================================================
-- Represents the individual seats purchased within an order.
-- Each item refers to an order and a seat managed by Sales
-- (sales_order_id, sales_seat_id) and stores the unit price,
-- the currency and the seat label applied by Sales at the
-- time of purchase.
-- Price, currency and label are kept as a historical snapshot
-- and do not change if the Catalog data is updated later.
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

     sales_order_id UUID NOT NULL,
     sales_seat_id UUID NOT NULL,
     sales_seat_label VARCHAR(50) NOT NULL,

     sales_unit_price VARCHAR(10) NOT NULL,
     sales_currency CHAR(3) NOT NULL DEFAULT 'EUR',

     CONSTRAINT fk_order_items_order
         FOREIGN KEY (sales_order_id)
             REFERENCES orders(id),

     CONSTRAINT fk_order_items_seat
         FOREIGN KEY (sales_seat_id)
             REFERENCES session_seats(id),

     CONSTRAINT uq_order_items_seat
         UNIQUE (sales_order_id, sales_seat_id),

     CONSTRAINT chk_order_items_unit_price
         CHECK (sales_unit_price ~ '^\d+(\.\d+)?$' AND sales_unit_price::NUMERIC >= 0),

     CONSTRAINT chk_order_items_currency
         CHECK (sales_currency IN ('EUR', 'USD', 'JPY', 'GBP'))
);


-- =====================================================
-- ORDER ITEMS INDEXES
-- =====================================================

CREATE INDEX idx_order_items_order
    ON order_items(sales_order_id);

CREATE INDEX idx_order_items_seat
    ON order_items(sales_seat_id);


-- Stores messages generated by the Sales service that must be published
-- to the message broker. Messages are inserted within the same transaction
-- as the corresponding business operation to ensure reliable publication.
-- The Publisher reads unpublished messages and updates published_at
-- after successful publication.
CREATE TABLE outgoing_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outgoing_messages_unpublished
    ON outgoing_messages (created_at)
    WHERE published_at IS NULL;

-- Stores messages received from other microservices to ensure
-- idempotent processing. Each message is recorded together with
-- its payload in the same transaction as the corresponding business
-- operations, preventing duplicate processing when a message is
-- delivered more than once.
-- id is the internal identifier generated by the database;
-- external_message_id is the identifier assigned by the sender
-- and guarantees idempotency through its UNIQUE constraint.
CREATE TABLE incoming_messages (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   external_message_id UUID NOT NULL,
   message_type VARCHAR(100) NOT NULL,
   payload JSONB NOT NULL,
   processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT uq_incoming_messages_external_message_id
       UNIQUE (external_message_id)
);
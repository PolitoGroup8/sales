-- =====================================================
-- SALES SESSIONS
-- =====================================================
-- Local snapshot of the sessions managed by the Catalog service.
-- Stores the event/session information required by Sales to
-- manage ticket availability and the sales window.
-- snapshot_version identifies the latest Catalog version
-- successfully applied to this local snapshot.

CREATE TABLE sales_sessions (
    id UUID PRIMARY KEY,

    catalog_event_id UUID NOT NULL,
    catalog_session_id UUID NOT NULL,
    catalog_venue_id UUID NOT NULL,

    catalog_session_start_at TIMESTAMPTZ NOT NULL,
    catalog_sales_start_at TIMESTAMPTZ NOT NULL,
    catalog_sales_end_at TIMESTAMPTZ NOT NULL,
    catalog_max_tickets_per_order INTEGER NOT NULL,

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
-- Does not store seat map or graphical information.

CREATE TABLE session_seats (
   id UUID PRIMARY KEY,

   sales_session_id UUID NOT NULL,
   catalog_seat_id UUID NOT NULL,

   status VARCHAR(10) NOT NULL DEFAULT 'FREE',
   catalog_price NUMERIC(10, 2) NOT NULL,
   catalog_currency CHAR(3) NOT NULL DEFAULT 'EUR',

   CONSTRAINT fk_session_seats_sales_session
       FOREIGN KEY (sales_session_id)
           REFERENCES sales_sessions(id),

   CONSTRAINT uq_session_seats_session_seat
       UNIQUE (sales_session_id, catalog_seat_id),

   CONSTRAINT chk_session_seats_status
       CHECK (status IN ('FREE', 'HELD', 'SOLD')),

   CONSTRAINT chk_session_seats_price
       CHECK (catalog_price >= 0),

   CONSTRAINT chk_session_seats_currency
       CHECK (catalog_currency IN ('EUR', 'USD', 'JPY', 'GBP'))
);


-- =====================================================
-- HOLDS
-- =====================================================
-- Represents a temporary reservation of one or more seats
-- made by a customer during the purchase process.
-- An ACTIVE hold reserves its seats until expiration.
-- ORDER_CREATED indicates that an order has been created
-- from the hold, while EXPIRED indicates an expired hold.

CREATE TABLE holds (
   id UUID PRIMARY KEY,

   customer_id UUID NOT NULL,
   status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
   expires_at TIMESTAMPTZ NOT NULL,

   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT chk_holds_status
       CHECK (status IN ('ACTIVE', 'ORDER_CREATED', 'EXPIRED'))
);


-- =====================================================
-- HOLD ITEMS
-- =====================================================
-- Represents the individual seats included in a hold.
-- Each item references a session seat managed by Sales.
-- A hold can contain multiple seats, but the same seat
-- cannot appear more than once within the same hold.

CREATE TABLE hold_items (
    id UUID PRIMARY KEY,

    hold_id UUID NOT NULL,
    seat_id UUID NOT NULL,

    CONSTRAINT fk_hold_items_hold
        FOREIGN KEY (hold_id)
            REFERENCES holds(id),

    CONSTRAINT fk_hold_items_seat
        FOREIGN KEY (seat_id)
            REFERENCES session_seats(id),

    CONSTRAINT uq_hold_items_hold_seat
        UNIQUE (hold_id, seat_id)
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
-- HOLDS INDEXES
-- =====================================================

CREATE INDEX idx_holds_customer
    ON holds(customer_id);

CREATE INDEX idx_holds_expiration
    ON holds(expires_at)
    WHERE status = 'ACTIVE';


-- =====================================================
-- HOLD ITEMS INDEXES
-- =====================================================

CREATE INDEX idx_hold_items_seat
    ON hold_items(seat_id);



-- =====================================================
-- ORDERS
-- =====================================================
-- Represents a customer's purchase created from a HOLD.
-- An order is created when the customer confirms the
-- selected seats and proceeds to the order summary.
-- Stores customer and price snapshots, payment information
-- and the current order status.
-- Payment processing is managed by the Payments service.

CREATE TABLE orders (
    id UUID PRIMARY KEY,

    hold_id UUID NOT NULL,

    customer_id UUID NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_first_name VARCHAR(100) NOT NULL,
    customer_last_name VARCHAR(100) NOT NULL,

    total_amount NUMERIC(10, 2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'EUR',

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',

    payment_expires_at TIMESTAMPTZ NOT NULL,
    payments_payment_id UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_hold
        FOREIGN KEY (hold_id)
            REFERENCES holds(id),

    CONSTRAINT uq_orders_hold
        UNIQUE (hold_id),

    CONSTRAINT chk_orders_total_amount
        CHECK (total_amount >= 0),

    CONSTRAINT chk_currency
        CHECK (
            currency IN (
                'EUR',
                'USD',
                'JPY',
                'GBP'
            )
        ),

    CONSTRAINT chk_orders_status
        CHECK (
            status IN (
                       'PENDING_PAYMENT',
                       'PAID',
                       'PAYMENT_FAILED',
                       'EXPIRED',
                       'CANCELLED'
                )
            )
);


-- =====================================================
-- ORDERS INDEXES
-- =====================================================

CREATE INDEX idx_orders_customer
    ON orders(customer_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_payment_expiration
    ON orders(payment_expires_at)
    WHERE status = 'PENDING_PAYMENT';



-- =====================================================
-- ORDER ITEMS
-- =====================================================
-- Represents the individual seats purchased within an order.
-- Each item refers to a seat managed by Sales and stores
-- the unit price applied at the time of purchase.
-- The price is kept as a historical snapshot and does not
-- change if the Catalog price is updated later.

CREATE TABLE order_items (
     id UUID PRIMARY KEY,

     order_id UUID NOT NULL,
     seat_id UUID NOT NULL,

     catalog_unit_price NUMERIC(10, 2) NOT NULL,
     currency CHAR(3) NOT NULL DEFAULT 'EUR',

     CONSTRAINT fk_order_items_order
         FOREIGN KEY (order_id)
             REFERENCES orders(id),

     CONSTRAINT fk_order_items_seat
         FOREIGN KEY (seat_id)
             REFERENCES session_seats(id),

     CONSTRAINT uq_order_items_seat
         UNIQUE (seat_id),

     CONSTRAINT chk_order_items_unit_price
         CHECK (catalog_unit_price >= 0),

     CONSTRAINT chk_order_items_currency
         CHECK (currency IN ('EUR', 'USD', 'JPY', 'GBP'))
);


-- =====================================================
-- ORDER ITEMS INDEXES
-- =====================================================

CREATE INDEX idx_order_items_order
    ON order_items(order_id);


-- Stores messages generated by the Sales service that must be published
-- to the message broker. Messages are inserted within the same transaction
-- as the corresponding business operation to ensure reliable publication.
-- The Publisher reads unpublished messages and updates published_at
-- after successful publication.
CREATE TABLE outgoing_messages (
    id UUID PRIMARY KEY,
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
CREATE TABLE incoming_messages (
   message_id UUID PRIMARY KEY,
   message_type VARCHAR(100) NOT NULL,
   payload JSONB NOT NULL,
   processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
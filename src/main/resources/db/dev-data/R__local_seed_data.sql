
-- =====================================================
-- INITIAL SEED DATA (TEST ENVIRONMENT)
-- =====================================================

-- =====================================================
-- 1. SALES SESSIONS
-- =====================================================

INSERT INTO sales_sessions (
    id,
    catalog_event_id,
    catalog_session_id,
    catalog_venue_id,
    catalog_session_start_at,
    catalog_sales_start_at,
    catalog_sales_end_at,
    catalog_max_tickets_per_order,
    snapshot_version
)
VALUES
    (
        '11111111-1111-1111-1111-111111111111',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        CURRENT_TIMESTAMP + INTERVAL '10 days',
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP + INTERVAL '9 days',
        4,
        1
    ),
    (
        '22222222-2222-2222-2222-222222222222',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        CURRENT_TIMESTAMP + INTERVAL '11 days',
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP + INTERVAL '10 days',
        6,
        1
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 2. SESSION SEATS
-- =====================================================

INSERT INTO session_seats (
    id,
    sales_session_id,
    catalog_seat_id,
    status,
    catalog_price,
    catalog_currency
)
VALUES

-- Session 1: FREE
(
    '33333333-3333-3333-3333-333333333331',
    '11111111-1111-1111-1111-111111111111',
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',
    'FREE',
    50.00,
    'EUR'
),

-- Session 1: HELD
(
    '33333333-3333-3333-3333-333333333332',
    '11111111-1111-1111-1111-111111111111',
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2',
    'HELD',
    50.00,
    'EUR'
),

-- Session 1: SOLD
(
    '33333333-3333-3333-3333-333333333333',
    '11111111-1111-1111-1111-111111111111',
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3',
    'SOLD',
    75.00,
    'EUR'
),

-- Session 2: FREE
(
    '33333333-3333-3333-3333-333333333334',
    '22222222-2222-2222-2222-222222222222',
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4',
    'FREE',
    40.00,
    'EUR'
),
(
    '33333333-3333-3333-3333-333333333335',
    '22222222-2222-2222-2222-222222222222',
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5',
    'FREE',
    40.00,
    'EUR'
)
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 3. HOLDS
-- =====================================================

INSERT INTO holds (
    id,
    customer_id,
    status,
    expires_at
)
VALUES
    (
        '44444444-4444-4444-4444-444444444441',
        '99999999-9999-9999-9999-999999999991',
        'ACTIVE',
        CURRENT_TIMESTAMP + INTERVAL '15 minutes'
    ),
    (
        '44444444-4444-4444-4444-444444444442',
        '99999999-9999-9999-9999-999999999992',
        'ORDER_CREATED',
        CURRENT_TIMESTAMP - INTERVAL '5 minutes'
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 4. HOLD ITEMS
-- =====================================================

INSERT INTO hold_items (
    id,
    hold_id,
    seat_id
)
VALUES
    (
        '55555555-5555-5555-5555-555555555551',
        '44444444-4444-4444-4444-444444444441',
        '33333333-3333-3333-3333-333333333332'
    ),
    (
        '55555555-5555-5555-5555-555555555552',
        '44444444-4444-4444-4444-444444444442',
        '33333333-3333-3333-3333-333333333333'
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 5. ORDERS
-- =====================================================

INSERT INTO orders (
    id,
    hold_id,
    customer_id,
    customer_email,
    customer_first_name,
    customer_last_name,
    total_amount,
    currency,
    status,
    payment_expires_at,
    payments_payment_id
)
VALUES
    (
        '66666666-6666-6666-6666-666666666661',
        '44444444-4444-4444-4444-444444444442',
        '99999999-9999-9999-9999-999999999992',
        'mario.rossi@example.test',
        'Mario',
        'Rossi',
        75.00,
        'EUR',
        'PAID',
        CURRENT_TIMESTAMP - INTERVAL '5 minutes',
        '77777777-7777-7777-7777-777777777771'
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 6. ORDER ITEMS
-- =====================================================

INSERT INTO order_items (
    id,
    order_id,
    seat_id,
    catalog_unit_price,
    currency
)
VALUES
    (
        '88888888-8888-8888-8888-888888888881',
        '66666666-6666-6666-6666-666666666661',
        '33333333-3333-3333-3333-333333333333',
        75.00,
        'EUR'
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 7. OUTGOING MESSAGES
-- =====================================================

INSERT INTO outgoing_messages (
    id,
    message_type,
    payload,
    published_at
)
VALUES
    (
        'aaaa1111-1111-1111-1111-111111111111',
        'OrderPaid',
        '{"orderId": "66666666-6666-6666-6666-666666666661", "totalAmount": 75.00, "currency": "EUR"}'::jsonb,
        CURRENT_TIMESTAMP
    ),
    (
        'aaaa2222-2222-2222-2222-222222222222',
        'SeatHeld',
        '{"holdId": "44444444-4444-4444-4444-444444444441", "seatId": "33333333-3333-3333-3333-333333333332"}'::jsonb,
        NULL
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 8. INCOMING MESSAGES
-- =====================================================

INSERT INTO incoming_messages (
    message_id,
    message_type,
    payload
)
VALUES
    (
        'bbbb1111-1111-1111-1111-111111111111',
        'CatalogSessionSynced',
        '{"catalogSessionId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", "snapshotVersion": 1}'::jsonb
    )
    ON CONFLICT DO NOTHING;
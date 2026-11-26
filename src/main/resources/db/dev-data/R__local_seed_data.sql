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
    catalog_event_name,
    catalog_session_start_at,
    catalog_sales_start_at,
    catalog_sales_end_at,
    catalog_max_tickets_per_order,
    catalog_venue_name,
    catalog_venue_city,
    snapshot_version
)
VALUES
    (
        '11111111-1111-1111-1111-111111111111',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'Concerto d''Autunno',
        '2026-12-05T17:04:00Z'::timestamptz,
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP + INTERVAL '9 days',
        4,
        'Teatro Regio',
        'Torino',
        1
    ),
    (
        '22222222-2222-2222-2222-222222222222',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'dddddddd-dddd-dddd-dddd-dddddddddddd',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'Concerto d''Autunno - Replica',
        '2026-12-06T17:04:00Z'::timestamptz,
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP + INTERVAL '10 days',
        6,
        'Teatro Regio',
        'Torino',
        1
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 2. SESSION SEATS
-- =====================================================
-- HELD seats are locked by an order in PENDING_PAYMENT.
-- SOLD seats belong to an order in PAID.

INSERT INTO session_seats (
    id,
    sales_session_id,
    catalog_sector_id,
    catalog_seat_id,
    catalog_seat_label,
    catalog_sector_name,
    status,
    catalog_price,
    catalog_currency
)
VALUES
    -- Session 1: FREE
    (
        '33333333-3333-3333-3333-333333333331',
        '11111111-1111-1111-1111-111111111111',
        'ffffffff-ffff-ffff-ffff-fffffffffff1',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',
        'A1',
        'Platea',
        'FREE',
        '50.00',
        'EUR'
    ),
    -- Session 1: HELD (order 66666666-...-662, PENDING_PAYMENT)
    (
        '33333333-3333-3333-3333-333333333332',
        '11111111-1111-1111-1111-111111111111',
        'ffffffff-ffff-ffff-ffff-fffffffffff1',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2',
        'A2',
        'Platea',
        'HELD',
        '50.00',
        'EUR'
    ),
    -- Session 1: SOLD (order 66666666-...-661, PAID)
    (
        '33333333-3333-3333-3333-333333333333',
        '11111111-1111-1111-1111-111111111111',
        'ffffffff-ffff-ffff-ffff-fffffffffff1',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3',
        'A3',
        'Platea',
        'SOLD',
        '75.00',
        'EUR'
    ),
    -- Session 2: FREE
    (
        '33333333-3333-3333-3333-333333333334',
        '22222222-2222-2222-2222-222222222222',
        'ffffffff-ffff-ffff-ffff-fffffffffff2',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4',
        'B1',
        'Galleria',
        'FREE',
        '40.00',
        'EUR'
    ),
    (
        '33333333-3333-3333-3333-333333333335',
        '22222222-2222-2222-2222-222222222222',
        'ffffffff-ffff-ffff-ffff-fffffffffff2',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5',
        'B2',
        'Galleria',
        'FREE',
        '40.00',
        'EUR'
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 3. ORDERS
-- =====================================================

INSERT INTO orders (
    id,
    sales_session_id,
    customer_id,
    total_amount,
    currency,
    status,
    expires_at
)
VALUES
    -- PAID order (seat A3 SOLD)
    (
        '66666666-6666-6666-6666-666666666661',
        '11111111-1111-1111-1111-111111111111',
        '99999999-9999-9999-9999-999999999992',
        '75.00',
        'EUR',
        'PAID',
        CURRENT_TIMESTAMP - INTERVAL '5 minutes'
    ),
    -- PENDING_PAYMENT order (seat A2 HELD)
    (
        '66666666-6666-6666-6666-666666666662',
        '11111111-1111-1111-1111-111111111111',
        '99999999-9999-9999-9999-999999999991',
        '50.00',
        'EUR',
        'PENDING_PAYMENT',
        CURRENT_TIMESTAMP + INTERVAL '10 minutes'
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 4. ORDER ITEMS
-- =====================================================

INSERT INTO order_items (
    id,
    sales_order_id,
    sales_seat_id,
    sales_seat_label,
    sales_unit_price,
    sales_currency
)
VALUES
    (
        '88888888-8888-8888-8888-888888888881',
        '66666666-6666-6666-6666-666666666661',
        '33333333-3333-3333-3333-333333333333',
        'A3',
        '75.00',
        'EUR'
    ),
    (
        '88888888-8888-8888-8888-888888888882',
        '66666666-6666-6666-6666-666666666662',
        '33333333-3333-3333-3333-333333333332',
        'A2',
        '50.00',
        'EUR'
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 5. ORDER PAYMENTS
-- =====================================================

INSERT INTO order_payments (
    id,
    sales_order_id,
    payments_payment_id
)
VALUES
    (
        '99999999-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
        '66666666-6666-6666-6666-666666666661',
        '77777777-7777-7777-7777-777777777771'
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 6. OUTGOING MESSAGES
-- =====================================================

-- =====================================================
-- 6. OUTGOING MESSAGES
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
        'ORDER_PAID',
        '{
          "type": "ORDER_PAID",
          "version": 1,
          "data": {
            "orderId": "66666666-6666-6666-6666-666666666661",
            "customerId": "99999999-9999-9999-9999-999999999992",
            "customerEmail": "user-99999999-9999-9999-9999-999999999992@example.com",
            "event": {
              "eventId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
              "sessionId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
              "eventTitle": "Concerto d''Autunno",
              "startsAt": "2026-12-05T17:04:00Z",
              "venueName": "Teatro Regio",
              "venueCity": "Torino"
            },
            "lines": [
              {
                "seatId": "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3",
                "seatLabel": "A3",
                "sectorId": "ffffffff-ffff-ffff-ffff-fffffffffff1",
                "sector": "Platea"
              },
              {
                "seatId": "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4",
                "seatLabel": "A4",
                "sectorId": "ffffffff-ffff-ffff-ffff-fffffffffff2",
                "sector": "Galleria"
              }
            ]
          }
        }'::jsonb,
        NULL
    )
    ON CONFLICT DO NOTHING;


-- =====================================================
-- 7. INCOMING MESSAGES
-- =====================================================

INSERT INTO incoming_messages (
    id,
    external_message_id,
    message_type,
    payload,
    processed_at
)
VALUES
    (
        'cccc1111-1111-1111-1111-111111111111',
        'bbbb1111-1111-1111-1111-111111111111',
        'CATALOG_SESSION_SYNC',
        '{"catalogSessionId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", "snapshotVersion": 1}'::jsonb,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT DO NOTHING;
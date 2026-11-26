# Test: simulating a valid `PAYMENT_SUCCESS`

Happy-path test using the seed data: order `...662` is `PENDING_PAYMENT`, seat A2 is `HELD`, and the total is 50.00 EUR.

## 1. PRE query (run before publishing)

```sql
SELECT
  (SELECT status FROM orders
    WHERE id = '66666666-6666-6666-6666-666666666662')            AS order_status,
  (SELECT status FROM session_seats
    WHERE id = '33333333-3333-3333-3333-333333333332')            AS seat_status,
  (SELECT COUNT(*) FROM order_payments
    WHERE sales_order_id = '66666666-6666-6666-6666-666666666662') AS order_payments_count,
  (SELECT COUNT(*) FROM incoming_messages
    WHERE external_message_id = '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d') AS incoming_messages_count;
```

Expected result:

| order_status | seat_status | order_payments_count | incoming_messages_count |
|---|---|---|---|
| PENDING_PAYMENT | HELD | 0 | 0 |

## 2. Publishing from the RabbitMQ management UI

Open `http://localhost:15672` → **Exchanges** → `payments-events` → **Publish message**.

| Field | Value |
|---|---|
| Routing key | `#` (or whichever key the `sales.payments-events` queue binding uses) |

### Properties

| Key | Value | Type |
|---|---|---|
| `content_type` | `application/json` | String |

### Headers

No header is required if the DTO polymorphism is resolved by the `type` field in the JSON (`@JsonTypeInfo`).

No headers needed

### Payload

```json
{
  "eventId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "occurredAt": "2026-11-20T20:30:00Z",
  "type": "PAYMENT_SUCCESS",
  "message": "Payment succeeded",
  "payload": {
    "amount": {
      "amount": "50.00",
      "currency": "EUR"
    },
    "orderId": "66666666-6666-6666-6666-666666666662",
    "paymentId": "77777777-7777-7777-7777-777777777772"
  }
}
```

## 3. POST query (same query as the PRE)

Expected result after processing:

| order_status | seat_status | order_payments_count | incoming_messages_count |
|---|---|---|---|
| PAID | SOLD | 1 | 1 |

Optional check of the order's `updated_at` and the stored payload:

```sql
SELECT status, updated_at
FROM orders
WHERE id = '66666666-6666-6666-6666-666666666662';

SELECT external_message_id, message_type, payload, processed_at
FROM incoming_messages
WHERE external_message_id = '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d';
```

## 4. Duplicate test

Publish **the exact same message** again. Expected:

- the logs show `Duplicate payment event ignored`;
- `order_payments_count` and `incoming_messages_count` stay at 1.

## 5. Use same order but different event id
```json
{
  "eventId": "99999999-9999-9999-9999-999999999999",
  "occurredAt": "2026-11-20T20:30:00Z",
  "type": "PAYMENT_SUCCESS",
  "message": "Payment succeeded",
  "payload": {
    "amount": {
      "amount": "50.00",
      "currency": "EUR"
    },
    "orderId": "66666666-6666-6666-6666-666666666662",
    "paymentId": "77777777-7777-7777-7777-777777777772"
  }
}
```

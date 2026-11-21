package it.polito.wa2.group8.sales.messaging.dto

import java.time.Instant
/**
 * {
 *   "eventId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
 *   "occurredAt": "2026-11-20T20:30:00Z",
 *   "status": "SUCCEEDED",
 *   "orderId": "ord_10492",
 *   "paymentId": "pay_88371",
 *   "amount": 75.00,
 *   "currency": "EUR"
 * }
 */
data class PaymentSucceededEventDTO(
    val eventId: String,
    val occurredAt: Instant,
    val status: String,
    val orderId: String,
    val paymentId: String,
    val amount: Int? = null,
    val currency: String? = null
)
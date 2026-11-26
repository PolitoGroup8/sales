package it.polito.wa2.group8.sales.messaging.dto

/**
 *{
 *   "eventId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
 *   "occurredAt": "2026-11-20T20:30:00Z",
 *   "type": "PAYMENT_SUCCESS",
 *   "message": "Payment succeeded",
 *   "payload": {
 *     "amount": {
 *       "amount": "75.00",
 *       "currency": "EUR"
 *     },
 *     "orderId": "66666666-6666-6666-6666-666666666661",
 *     "paymentId": "pay_88371"
 *   }
 * }
 */


data class PaymentSucceededEventDTO(
    override val type: String,
    override val message: String,
    override val eventId: String,
    override val occurredAt: String,
    val payload: PaymentSucceededDTO,
) : RabbitEventDTO

data class PaymentSucceededDTO(
    val amount: MoneyDTO,
    val orderId: String,
    val paymentId: String
)

data class MoneyDTO(
    val amount: String,
    val currency: String,
)

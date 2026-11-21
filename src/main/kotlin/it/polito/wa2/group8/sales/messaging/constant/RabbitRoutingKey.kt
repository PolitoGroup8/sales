package it.polito.wa2.group8.sales.messaging.constant

enum class RabbitRoutingKey(
    val value: String
) {
    PAYMENT_CREATED("payment.created")
}
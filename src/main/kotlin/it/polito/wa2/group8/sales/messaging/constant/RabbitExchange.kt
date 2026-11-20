package it.polito.wa2.group8.sales.messaging.constant

enum class RabbitExchange(
    val exchangeName: String
) {
    PAYMENTS_EVENTS("payments-events")
}
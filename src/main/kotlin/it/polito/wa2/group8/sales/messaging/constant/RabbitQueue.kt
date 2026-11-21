package it.polito.wa2.group8.sales.messaging.constant

object RabbitQueue {
    const val PAYMENTS_EVENTS = "sales.payments-events"
    const val ORDERS_EVENTS = "sales.orders-events"
    const val REFUNDS_EVENTS = "sales.refunds-events"
}
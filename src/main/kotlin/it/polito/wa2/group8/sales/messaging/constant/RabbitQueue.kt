package it.polito.wa2.group8.sales.messaging.constant

interface RabbitMessaging {
    companion object {
        const val PAYMENTS_EVENTS_QUEUE = "sales.payments-events"
        const val ORDERS_EVENTS_EXCHANGE = "orders-events"
        const val ORDER_PAID_ROUTING_KEY = "order.paid"
    }
}
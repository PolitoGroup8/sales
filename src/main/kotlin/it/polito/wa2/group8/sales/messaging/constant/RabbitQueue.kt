package it.polito.wa2.group8.sales.messaging.constant

object RabbitExchanges {
    const val ORDERS_EVENTS_EXCHANGE = "orders-events"
}

object RabbitRoutingKeys {
    const val ORDER_PAID_ROUTING_KEY = "order.paid"
}

object RabbitQueues {
    const val PAYMENTS_EVENTS_QUEUE = "sales.payments-events"
}
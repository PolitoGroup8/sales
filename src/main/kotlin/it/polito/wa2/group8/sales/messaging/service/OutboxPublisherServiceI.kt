package it.polito.wa2.group8.sales.messaging.service

interface OutboxPublisherServiceI {
    fun publishPendingMessages()
}
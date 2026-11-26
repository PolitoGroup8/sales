package it.polito.wa2.group8.sales.messaging.sender

import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import it.polito.wa2.group8.sales.messaging.constant.RabbitMessaging
import it.polito.wa2.group8.sales.services.OutgoingMessagesServiceI
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxPublisher(
    private val outgoingMessagesService: OutgoingMessagesServiceI,
    private val rabbitMessageSender: RabbitMessageSender
) {

    private val logger = LoggerFactory.getLogger(OutboxPublisher::class.java)

    @Scheduled(fixedDelayString = $$"${app.outbox.poll-delay-ms:1000}")
    fun publishPendingMessages() {
        val pendingMessages = outgoingMessagesService.findPendingMessages()
        if (pendingMessages.isEmpty()) {
            logger.trace("No pending outgoing messages found")
            return
        }

        for ((id, messageType, payloadJson) in pendingMessages) {
            try {
                val (exchange, routingKey) = routeFor(messageType) ?: run {
                    logger.warn("No RabbitMQ route configured for messageType={} id={}", messageType, id)
                    continue
                }

                rabbitMessageSender.sendJson(
                    exchange = exchange,
                    routingKey = routingKey,
                    messageId = id.value.toString(),
                    messageType = messageType.wireValue,
                    jsonPayload = payloadJson
                )

                outgoingMessagesService.markAsPublished(id)
                logger.debug("Published outgoing message id={} type={}", id, messageType)
            } catch (ex: Exception) {
                logger.error(
                    "Failed to publish outgoing message id={} type={}. Will retry later.",
                    id,
                    messageType,
                    ex
                )
            }
        }
    }

    private fun routeFor(messageType: MessageTypes): Pair<String, String>? = when (messageType) {
        MessageTypes.ORDER_PAID -> RabbitMessaging.ORDERS_EVENTS_EXCHANGE to RabbitMessaging.ORDER_PAID_ROUTING_KEY
        else -> null
    }
}

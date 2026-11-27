package it.polito.wa2.group8.sales.messaging.service.impl

import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import it.polito.wa2.group8.sales.messaging.constant.RabbitExchanges
import it.polito.wa2.group8.sales.messaging.constant.RabbitRoutingKeys
import it.polito.wa2.group8.sales.messaging.sender.RabbitMessageSender
import it.polito.wa2.group8.sales.messaging.service.OutboxPublisherServiceI
import it.polito.wa2.group8.sales.services.OutgoingMessagesServiceI
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class OutboxPublisherService(
    private val outgoingMessagesService: OutgoingMessagesServiceI,
    private val rabbitMessageSender: RabbitMessageSender
) : OutboxPublisherServiceI {

    private val logger = LoggerFactory.getLogger(OutboxPublisherService::class.java)

    @Scheduled(fixedDelayString = $$"${app.outbox.poll-delay-ms:1000}")
    override fun publishPendingMessages() {
        val pendingMessages = outgoingMessagesService.findPendingMessages()
        if (pendingMessages.isNotEmpty()) {
            for ((id, messageType, payloadJson) in pendingMessages) {
                try {
                    when (messageType) {
                        MessageTypes.ORDER_PAID -> publishMessage(
                            id = id,
                            messageType = messageType,
                            payloadJson = payloadJson,
                            exchange = RabbitExchanges.ORDERS_EVENTS_EXCHANGE,
                            routingKey = RabbitRoutingKeys.ORDER_PAID_ROUTING_KEY
                        )
                        else -> {
                            logger.warn(
                                "Unsupported outgoing message type={} for id={}, marking as published",
                                messageType,
                                id
                            )
                        }
                    }

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
        } else {
            logger.trace("No pending outgoing messages found")
        }
    }

    private fun publishMessage(
        id: EntityId,
        messageType: MessageTypes,
        payloadJson: String,
        exchange: String,
        routingKey: String
    ) {
        rabbitMessageSender.sendJson(
            exchange = exchange,
            routingKey = routingKey,
            messageId = id.value.toString(),
            messageType = messageType.wireValue,
            jsonPayload = payloadJson
        )

        outgoingMessagesService.markAsPublished(id)
    }
}
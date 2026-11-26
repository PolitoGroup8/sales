package it.polito.wa2.group8.sales.messaging.sender

import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class RabbitMessageSender(
    private val rabbitTemplate: RabbitTemplate
) {
    @Throws(AmqpException::class)
    fun sendJson(
        exchange: String,
        routingKey: String,
        messageId: String,
        messageType: String,
        jsonPayload: String
    ) {
        val message: Message = MessageBuilder
            .withBody(jsonPayload.toByteArray(StandardCharsets.UTF_8))
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .setMessageId(messageId)
            .setHeader("messageType", messageType)
            .build()

        rabbitTemplate.invoke {
            rabbitTemplate.send(exchange, routingKey, message)
            val confirmed = rabbitTemplate.waitForConfirms(5000)
            if (!confirmed) {
                throw AmqpException("RabbitMQ broker did not acknowledge message id=$messageId on exchange=$exchange")
            }
        }
    }
}
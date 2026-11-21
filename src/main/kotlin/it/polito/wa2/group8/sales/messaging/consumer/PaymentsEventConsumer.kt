package it.polito.wa2.group8.sales.messaging.consumer

import it.polito.wa2.group8.sales.messaging.constant.RabbitQueue
import it.polito.wa2.group8.sales.messaging.dto.PaymentSucceededEventDTO
import it.polito.wa2.group8.sales.messaging.service.PaymentsEventHandlerI
import it.polito.wa2.group8.sales.messaging.service.impl.PaymentsEventHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class PaymentsEventConsumer(
    private val objectMapper: ObjectMapper,
    private val paymentsEventHandler: PaymentsEventHandlerI,
) {

    @RabbitListener(queues = [RabbitQueue.PAYMENTS_EVENTS])
    fun consume(message: String) {
        val event = objectMapper.readValue(message, PaymentSucceededEventDTO::class.java)
        paymentsEventHandler.handleMessage(event)
    }
}
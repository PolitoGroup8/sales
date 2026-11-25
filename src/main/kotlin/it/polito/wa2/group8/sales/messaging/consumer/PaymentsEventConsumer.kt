package it.polito.wa2.group8.sales.messaging.consumer

import it.polito.wa2.group8.sales.messaging.constant.RabbitQueue
import it.polito.wa2.group8.sales.messaging.dto.PaymentSucceededEventDTO
import it.polito.wa2.group8.sales.messaging.dto.RabbitEventDTO
import it.polito.wa2.group8.sales.messaging.mapper.toDomain

import it.polito.wa2.group8.sales.messaging.service.PaymentsEventHandlerI
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class PaymentsEventConsumer(
    private val paymentsEventHandler: PaymentsEventHandlerI,
) {

    @RabbitListener(queues = [RabbitQueue.PAYMENTS_EVENTS])
    fun consume(rabbitEventDTO: RabbitEventDTO) {
        when (rabbitEventDTO) {
            is PaymentSucceededEventDTO -> {
                val paymentSucceededEvent = rabbitEventDTO.toDomain()
                paymentsEventHandler.handlePaymentSuccess(paymentSucceededEvent)
            }
            else -> ""
        }
    }
}
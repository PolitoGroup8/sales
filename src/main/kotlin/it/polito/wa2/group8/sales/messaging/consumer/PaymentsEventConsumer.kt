package it.polito.wa2.group8.sales.messaging.consumer

import it.polito.wa2.group8.sales.exceptions.OrderDataAnomalyException
import it.polito.wa2.group8.sales.messaging.constant.RabbitQueues
import it.polito.wa2.group8.sales.messaging.dto.PaymentSucceededEventDTO
import it.polito.wa2.group8.sales.messaging.dto.RabbitEventDTO
import it.polito.wa2.group8.sales.messaging.dto.UnknownEventDTO
import it.polito.wa2.group8.sales.messaging.mapper.toDomain
import it.polito.wa2.group8.sales.messaging.service.PaymentsEventHandlerI
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import tools.jackson.core.JacksonException

@Component
class PaymentsEventConsumer(
    private val paymentsEventHandler: PaymentsEventHandlerI,
) {
    private val logger = LoggerFactory.getLogger(PaymentsEventConsumer::class.java)

    @RabbitListener(queues = [RabbitQueues.PAYMENTS_EVENTS_QUEUE])
    fun consume(rabbitEventDTO: RabbitEventDTO) {
        try {
            when (rabbitEventDTO) {
                is PaymentSucceededEventDTO ->
                    paymentsEventHandler.handlePaymentSuccess(
                        rabbitEventDTO.toDomain()
                    )
                is UnknownEventDTO -> logger.warn(
                    "Event ignored, type='{}', eventId={}",
                    rabbitEventDTO.type.ifBlank { "<missing>" },
                    rabbitEventDTO.eventId,
                )
            }
        } catch (ex: Exception) {
            when (ex) {
                is OrderDataAnomalyException,
                is IllegalArgumentException -> {
                    logger.error("Non-retryable error consuming eventId={}: {}", rabbitEventDTO.eventId, ex.message, ex)
                    ex.message?.let { throw AmqpRejectAndDontRequeueException(it, ex) }
                }
                is JacksonException -> {
                    logger.error("Error serializing object", ex)
                }
                else -> throw ex
            }
        }
    }
}
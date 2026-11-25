package it.polito.wa2.group8.sales.messaging.service.impl


import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceededEvent
import it.polito.wa2.group8.sales.messaging.mapper.toIncomingMessage
import it.polito.wa2.group8.sales.messaging.service.PaymentsEventHandlerI
import it.polito.wa2.group8.sales.services.IncomingMessagesServiceI
import it.polito.wa2.group8.sales.services.OrderServiceI
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentsEventHandler(
    private val orderService: OrderServiceI,
    private val incomingMessagesService: IncomingMessagesServiceI
) : PaymentsEventHandlerI {

    private val logger = LoggerFactory.getLogger(PaymentsEventHandler::class.java)

    @Transactional
    override fun handlePaymentSuccess(paymentSucceededEvent: PaymentSucceededEvent) {
        val inserted = incomingMessagesService
            .insertIfAbsent(paymentSucceededEvent.toIncomingMessage())
        if (inserted) {
            orderService.holdPaymentSucceeded(paymentSucceededEvent.payload)
        }
        else{
            logger.info(
                "Duplicate payment event ignored. eventId={}",
                paymentSucceededEvent.eventId
            )
        }
    }
}
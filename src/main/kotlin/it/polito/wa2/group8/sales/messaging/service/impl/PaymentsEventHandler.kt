package it.polito.wa2.group8.sales.messaging.service.impl


import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceeded

import it.polito.wa2.group8.sales.messaging.service.PaymentsEventHandlerI
import it.polito.wa2.group8.sales.services.impl.OrderService
import org.springframework.stereotype.Service

@Service
class PaymentsEventHandler(
    private val orderService: OrderService
) : PaymentsEventHandlerI {
    override fun handlePaymentSuccess(paymentSucceeded: PaymentSucceeded) {
        orderService.holdPaymentSucceded(paymentSucceeded)
    }
}
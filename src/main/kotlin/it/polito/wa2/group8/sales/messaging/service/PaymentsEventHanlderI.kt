package it.polito.wa2.group8.sales.messaging.service

import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceededEvent

interface PaymentsEventHandlerI {
    fun handlePaymentSuccess(paymentSucceededEvent: PaymentSucceededEvent)
}

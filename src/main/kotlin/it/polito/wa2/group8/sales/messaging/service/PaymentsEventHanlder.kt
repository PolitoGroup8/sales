package it.polito.wa2.group8.sales.messaging.service

import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceeded


interface PaymentsEventHandlerI {
    fun handlePaymentSuccess(paymentSucceeded: PaymentSucceeded)
}

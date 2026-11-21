package it.polito.wa2.group8.sales.messaging.service

import it.polito.wa2.group8.sales.messaging.dto.PaymentSucceededEventDTO

interface PaymentsEventHandlerI {
    fun handleMessage(paymentSucceededDTO: PaymentSucceededEventDTO)
}

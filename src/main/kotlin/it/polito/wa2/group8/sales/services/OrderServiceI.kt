package it.polito.wa2.group8.sales.services

import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceeded

interface OrderServiceI {
    fun holdPaymentSucceded(paymentSucceeded: PaymentSucceeded)
}
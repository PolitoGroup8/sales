package it.polito.wa2.group8.sales.services

import it.polito.wa2.group8.sales.domain.messages.incoming.IMPaymentSucceeded

interface IncomingMessagesServiceI {
    fun insertIfAbsent(imPaymentSucceeded: IMPaymentSucceeded): Boolean
}
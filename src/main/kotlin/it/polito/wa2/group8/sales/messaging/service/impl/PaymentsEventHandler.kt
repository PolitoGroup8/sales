package it.polito.wa2.group8.sales.messaging.service.impl

import it.polito.wa2.group8.sales.messaging.dto.PaymentSucceededEventDTO
import it.polito.wa2.group8.sales.messaging.service.PaymentsEventHandlerI
import org.springframework.stereotype.Service

@Service
class PaymentsEventHandler: PaymentsEventHandlerI {
    override fun handleMessage(paymentSucceededDTO: PaymentSucceededEventDTO) {
        println("handleMessage")
        print(paymentSucceededDTO)
    }
}
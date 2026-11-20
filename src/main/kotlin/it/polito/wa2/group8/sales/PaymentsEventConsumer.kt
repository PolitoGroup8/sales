package it.polito.wa2.group8.sales

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class PaymentsEventConsumer {

    @RabbitListener(queues = ["sales.payments-events"])
    fun consume(message: String) {
        println("================================")
        println("PAYMENT EVENT RECEIVED")
        println(message)
        println("================================")
    }
}
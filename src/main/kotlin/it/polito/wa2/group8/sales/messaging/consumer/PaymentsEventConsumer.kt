package it.polito.wa2.group8.sales.messaging.consumer

import it.polito.wa2.group8.sales.messaging.constant.RabbitQueue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class PaymentsEventConsumer {

    @RabbitListener(queues = [RabbitQueue.PAYMENTS_EVENTS])
    fun consume(message: String) {
        println("================================")
        println("PAYMENT EVENT RECEIVED")
        println(message)
        println("================================")
    }
}
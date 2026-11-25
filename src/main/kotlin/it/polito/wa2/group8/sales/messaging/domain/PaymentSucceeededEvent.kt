package it.polito.wa2.group8.sales.messaging.domain

import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import kotlin.time.Instant


data class PaymentSucceededEvent(
    override val eventId: EntityId,
    override val occurredAt: Instant,
    override val message: String,
    override val type: MessageTypes,
    val payload: PaymentSucceeded
) : RabbitEvent

data class PaymentSucceeded(
    val amount: Money,
    val orderId: EntityId,
    val paymentId: EntityId
)
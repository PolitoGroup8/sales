package it.polito.wa2.group8.sales.messaging.domain

import it.polito.wa2.group8.sales.domain.types.EntityId
import java.time.Instant

sealed interface RabbitEvent {
    val eventId: EntityId
    val occurredAt: Instant
    val message: String
}

data class PaymentSucceededEvent(
    override val eventId: EntityId,
    override val occurredAt: Instant,
    override val message: String,
    val payload: PaymentSucceeded
) : RabbitEvent

data class PaymentSucceeded(
    val amount: Money,
    val orderId: EntityId,
    val paymentId: EntityId
)
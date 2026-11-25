package it.polito.wa2.group8.sales.messaging.mapper

import it.polito.wa2.group8.sales.domain.Currency
import it.polito.wa2.group8.sales.domain.types.Amount
import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import it.polito.wa2.group8.sales.messaging.domain.Money
import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceeded
import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceededEvent
import it.polito.wa2.group8.sales.messaging.dto.PaymentSucceededDTO
import it.polito.wa2.group8.sales.messaging.dto.PaymentSucceededEventDTO
import kotlin.time.Instant

fun PaymentSucceededDTO.toDomain(): PaymentSucceeded {
    return PaymentSucceeded(
        orderId = EntityId.fromString(this.orderId),
        amount = Money(
            Currency.from(this.amount.currency),
            Amount(this.amount.amount)
        ),
        paymentId = EntityId.fromString(this.paymentId)
    )
}

fun PaymentSucceededEventDTO.toDomain(): PaymentSucceededEvent {
    return PaymentSucceededEvent(
        eventId = EntityId.fromString(this.eventId),
        occurredAt = Instant.parse(this.occurredAt),
        message = this.message,
        payload = this.payload.toDomain(),
        type = MessageTypes.from(this.type)
    )
}

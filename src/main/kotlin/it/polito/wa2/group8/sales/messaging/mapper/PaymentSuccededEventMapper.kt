package it.polito.wa2.group8.sales.messaging.mapper

import it.polito.wa2.group8.sales.domain.types.Amount
import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.domain.Currency
import it.polito.wa2.group8.sales.messaging.domain.Money
import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceeded
import it.polito.wa2.group8.sales.messaging.dto.PaymentSucceededDTO

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
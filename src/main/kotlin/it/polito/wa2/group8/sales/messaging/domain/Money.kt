package it.polito.wa2.group8.sales.messaging.domain

import it.polito.wa2.group8.sales.domain.Currency
import it.polito.wa2.group8.sales.domain.types.Amount

data class Money(
    val currency: Currency,
    val amount: Amount
)


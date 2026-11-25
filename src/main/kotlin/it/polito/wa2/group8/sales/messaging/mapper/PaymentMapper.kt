package it.polito.wa2.group8.sales.messaging.mapper

import it.polito.wa2.group8.sales.domain.messages.incoming.IMPaymentSucceeded
import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceededEvent
import java.time.OffsetDateTime


fun PaymentSucceededEvent.toIncomingMessage(): IMPaymentSucceeded {
    return IMPaymentSucceeded(
        externalMessageId = this.eventId,
        messageType = this.type,
        payload = this.payload,
        processedAt = OffsetDateTime.now()
    )
}
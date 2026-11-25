package it.polito.wa2.group8.sales.domain.messages.incoming

import it.polito.wa2.group8.sales.domain.IncomingMessageI
import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceeded
import java.time.OffsetDateTime

data class IMPaymentSucceeded(
    override val id: EntityId? = null,
    override val externalMessageId: EntityId,
    override val messageType: MessageTypes,
    override val processedAt: OffsetDateTime,
    val payload: PaymentSucceeded,
) : IncomingMessageI
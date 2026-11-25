package it.polito.wa2.group8.sales.mapper

import it.polito.wa2.group8.sales.db.entities.IncomingMessageEntity
import it.polito.wa2.group8.sales.domain.messages.incoming.IMPaymentSucceeded
import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceeded
import java.util.UUID

fun IMPaymentSucceeded.toEntity(
    payloadJson: String
): IncomingMessageEntity {
    return IncomingMessageEntity(
        externalMessageId = externalMessageId.value,
        messageType = messageType.name,
        payload = payloadJson,
        processedAt = processedAt
    )
}

fun IncomingMessageEntity.toDomain(
    payload: PaymentSucceeded,
    id: UUID? = null
): IMPaymentSucceeded {
    return IMPaymentSucceeded(
        id = id?.let(::EntityId),
        externalMessageId = EntityId(externalMessageId),
        messageType = MessageTypes.valueOf(messageType),
        payload = payload,
        processedAt = processedAt
    )
}
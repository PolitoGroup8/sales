package it.polito.wa2.group8.sales.mapper

import it.polito.wa2.group8.sales.db.entities.OutgoingMessagesEntity
import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketPayload
import it.polito.wa2.group8.sales.domain.messages.outgoing.OMPaymentSucceeded
import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes

fun OMPaymentSucceeded.toEntity(
    payloadJson: String
): OutgoingMessagesEntity {
    return OutgoingMessagesEntity(
        messageType = messageType.name,
        payload = payloadJson,
        createdAt = createdAt,
        publishedAt = publishedAt
    )
}

fun OutgoingMessagesEntity.toDomain(
    payload: IssueTicketPayload
): OMPaymentSucceeded {
    return OMPaymentSucceeded(
        id = this.id?.let(::EntityId),
        messageType = MessageTypes.valueOf(messageType),
        createdAt = createdAt,
        publishedAt = publishedAt,
        payload = payload
    )
}

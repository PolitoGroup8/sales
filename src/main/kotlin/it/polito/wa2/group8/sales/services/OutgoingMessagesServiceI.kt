package it.polito.wa2.group8.sales.services

import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketPayload
import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import java.time.OffsetDateTime

data class PendingOutgoingMessage(
    val id: EntityId,
    val messageType: MessageTypes,
    val payloadJson: String
)

interface OutgoingMessagesServiceI {
    fun recordOrderPaid(
        payload: IssueTicketPayload,
        occurredAt: OffsetDateTime = OffsetDateTime.now()
    )

    fun findPendingMessages(): List<PendingOutgoingMessage>

    fun markAsPublished(
        messageId: EntityId,
        publishedAt: OffsetDateTime = OffsetDateTime.now()
    )
}

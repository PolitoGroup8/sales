package it.polito.wa2.group8.sales.domain.messages.outgoing

import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import java.time.OffsetDateTime

sealed interface OutgoingMessageI {
    val id: EntityId?
    val messageType: MessageTypes
    val createdAt: OffsetDateTime
    val publishedAt: OffsetDateTime?
}

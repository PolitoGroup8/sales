package it.polito.wa2.group8.sales.domain.messages.interfaces


import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import java.time.OffsetDateTime

interface IncomingMessageI {
    val id: EntityId?
    val externalMessageId: EntityId
    val messageType: MessageTypes
    val processedAt: OffsetDateTime
}


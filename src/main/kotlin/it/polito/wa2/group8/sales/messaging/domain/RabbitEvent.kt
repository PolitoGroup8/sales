package it.polito.wa2.group8.sales.messaging.domain

import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import kotlin.time.Instant

sealed interface RabbitEvent {
    val eventId: EntityId
    val occurredAt: Instant
    val message: String
    val type: MessageTypes
}

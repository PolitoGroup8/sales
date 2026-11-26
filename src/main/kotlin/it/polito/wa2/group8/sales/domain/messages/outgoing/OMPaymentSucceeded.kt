package it.polito.wa2.group8.sales.domain.messages.outgoing

import it.polito.wa2.group8.sales.domain.types.Email
import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import kotlin.time.Instant
import java.time.OffsetDateTime

data class OMPaymentSucceeded(
    override val id: EntityId?,
    override val messageType: MessageTypes,
    override val createdAt: OffsetDateTime,
    override val publishedAt: OffsetDateTime?,
    val payload: IssueTicketPayload
): OutgoingMessageI

data class IssueTicketPayload(
    val type: MessageTypes = MessageTypes.ORDER_PAID,
    val version: Int = 1,
    val data: IssueTicketData
)

data class IssueTicketData(
    val orderId: EntityId,
    val customerId: EntityId,
    val customerEmail: Email,
    val event: IssueTicketEvent,
    val lines: List<IssueTicketLine>
)

data class IssueTicketEvent(
    val eventId: EntityId,
    val sessionId: EntityId,
    val eventTitle: String,
    val startsAt: Instant,
    val venueName: String,
    val venueCity: String
)

data class IssueTicketLine(
    val catalogSeatId: EntityId,
    val catalogSeatLabel: String,
    val catalogSectorId: EntityId,
    val sector: String
)
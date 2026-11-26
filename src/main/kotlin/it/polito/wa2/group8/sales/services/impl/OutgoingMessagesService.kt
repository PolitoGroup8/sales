package it.polito.wa2.group8.sales.services.impl

import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketPayload
import it.polito.wa2.group8.sales.domain.messages.outgoing.OMPaymentSucceeded
import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.mapper.toEntity
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import it.polito.wa2.group8.sales.messaging.mapper.toDTO
import it.polito.wa2.group8.sales.repositories.OutgoingMessagesRepoI
import it.polito.wa2.group8.sales.services.OutgoingMessagesServiceI
import it.polito.wa2.group8.sales.services.PendingOutgoingMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.time.OffsetDateTime

@Service
class OutgoingMessagesService(
    private val outgoingMessagesRepo: OutgoingMessagesRepoI,
    private val objectMapper: ObjectMapper
) : OutgoingMessagesServiceI {

    override fun recordOrderPaid(
        payload: IssueTicketPayload,
        occurredAt: OffsetDateTime
    ) {
        val oMPaymentSucceeded = OMPaymentSucceeded(
            id = null,
            messageType = MessageTypes.ORDER_PAID,
            createdAt = occurredAt,
            publishedAt = null,
            payload = payload
        )

        val oMPaymentSucceededJson = objectMapper.writeValueAsString(payload.toDTO())
        val outboxEntity = oMPaymentSucceeded.toEntity(oMPaymentSucceededJson)

        outgoingMessagesRepo.save(outboxEntity)
    }

    override fun findPendingMessages(): List<PendingOutgoingMessage> {
        return outgoingMessagesRepo.findAllByPublishedAtIsNullOrderByCreatedAtAsc()
            .mapNotNull { entity ->
                val id = entity.id?.let(::EntityId) ?: return@mapNotNull null
                val type = MessageTypes.from(entity.messageType)
                if (type == MessageTypes.UNKNOWN) return@mapNotNull null
                PendingOutgoingMessage(
                    id = id,
                    messageType = type,
                    payloadJson = entity.payload
                )
            }
    }

    override fun markAsPublished(
        messageId: EntityId,
        publishedAt: OffsetDateTime
    ) {
        val entity = outgoingMessagesRepo.findById(messageId.value)
            .orElseThrow { IllegalStateException("Outgoing message not found for id=${messageId.value}") }
        entity.publishedAt = publishedAt
        outgoingMessagesRepo.save(entity)
    }
}

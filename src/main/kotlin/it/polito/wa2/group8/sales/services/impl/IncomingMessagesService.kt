package it.polito.wa2.group8.sales.services.impl

import it.polito.wa2.group8.sales.domain.messages.incoming.IMPaymentSucceeded
import it.polito.wa2.group8.sales.mapper.toEntity
import it.polito.wa2.group8.sales.repositories.IncomingMessagesRepoI
import it.polito.wa2.group8.sales.services.IncomingMessagesServiceI
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper

@Service
class IncomingMessagesService(
    private val incomingMessagesRepo: IncomingMessagesRepoI,
    private val jsonMapper: JsonMapper,
) : IncomingMessagesServiceI {

    override fun insertIfAbsent(
        imPaymentSucceeded: IMPaymentSucceeded): Boolean {
        val payloadJson = jsonMapper.writeValueAsString(
            imPaymentSucceeded.payload
        )

        val entity = imPaymentSucceeded.toEntity(
            payloadJson = payloadJson
        )

        return incomingMessagesRepo.insertIfAbsent(
            externalMessageId = entity.externalMessageId,
            messageType = entity.messageType,
            payload = entity.payload,
            processedAt = entity.processedAt,
        ) == 1
    }
}
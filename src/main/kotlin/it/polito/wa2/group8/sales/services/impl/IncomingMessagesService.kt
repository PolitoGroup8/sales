package it.polito.wa2.group8.sales.services.impl

import it.polito.wa2.group8.sales.domain.messages.incoming.IMPaymentSucceeded
import it.polito.wa2.group8.sales.repositories.IncomingMessagesRepoI
import it.polito.wa2.group8.sales.services.IncomingMessagesServiceI
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper

@Service
class IncomingMessagesService(
    private val incomingMessagesRepo: IncomingMessagesRepoI,
    private val jsonMapper: JsonMapper,
) : IncomingMessagesServiceI {

    override fun insertIfAbsent(imPaymentSucceeded: IMPaymentSucceeded): Boolean {
        val payloadJson = jsonMapper.writeValueAsString(imPaymentSucceeded.payload)

        val linesAffected = incomingMessagesRepo.insertIfAbsent(
            externalMessageId = imPaymentSucceeded.externalMessageId.value,
            messageType = imPaymentSucceeded.messageType.name,
            payload = payloadJson,
            processedAt = imPaymentSucceeded.processedAt,
        )

        return linesAffected == 1
    }
}
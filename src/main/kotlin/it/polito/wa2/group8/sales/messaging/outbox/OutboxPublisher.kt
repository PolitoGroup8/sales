package it.polito.wa2.group8.sales.messaging.outbox

import it.polito.wa2.group8.sales.db.entities.OutgoingMessagesEntity
import it.polito.wa2.group8.sales.repositorys.OutgoingMessagesRepoI
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageDeliveryMode
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

/**
 * Publishes the rows of outgoing_messages to RabbitMQ (relay side of the Transactional Outbox).
 *
 * The business transaction (e.g. order -> PAID) only INSERTs a row in outgoing_messages.
 * This class later reads the unpublished rows, sends them to the broker and sets
 * published_at once the broker has confirmed them.
 *
 * Delivery guarantee: at-least-once. If Sales stops after the broker confirmed a message
 * but before the transaction that sets published_at commits, the message is sent again
 * at the next run. Consumers must therefore deduplicate (AMQP message-id = outbox row id).
 */
@Component
class OutboxPublisher(
    private val outgoingMessagesRepo: OutgoingMessagesRepoI,
    private val rabbitTemplate: RabbitTemplate,
    private val properties: OutboxProperties,
) {

    private val logger = LoggerFactory.getLogger(OutboxPublisher::class.java)

    /**
     * Publishes up to `batchSize` pending messages, oldest first.
     *
     * The rows are locked for the whole transaction (FOR UPDATE SKIP LOCKED), so two
     * concurrent runs never publish the same row. Messages are sent one at a time, waiting
     * for each confirm: at the first failure the run stops, so that a later message is never
     * published before an earlier one. Messages published before the failure stay published.
     *
     * @return the number of messages published in this run.
     */
    @Transactional
    fun publishPending(): Int {
        val pending = outgoingMessagesRepo.findUnpublishedForUpdate(properties.batchSize)
        var published = 0

        for (outgoing in pending) {
            val routingKey = properties.routingKeys[outgoing.messageType]
            if (routingKey == null) {
                // Configuration error: the row stays unpublished and is retried at every run.
                logger.error(
                    "No routing key configured for message type '{}' (sales.outbox.routing-keys). Message {} not published.",
                    outgoing.messageType, outgoing.id,
                )
                continue
            }

            try {
                send(outgoing, routingKey)
            } catch (e: OutboxPublishException) {
                logger.warn("Outbox message {} not published, will retry: {}", outgoing.id, e.message)
                break
            }

            // Dirty-checked by JPA: the UPDATE is flushed when the transaction commits.
            outgoing.publishedAt = OffsetDateTime.now()
            published++
        }

        if (published > 0) {
            logger.info("Published {} outbox message(s) on exchange '{}'", published, properties.exchange)
        }
        return published
    }

    /**
     * Sends one message and waits for the broker's confirm.
     *
     * Throws [OutboxPublishException] if the broker is unreachable, does not confirm in time,
     * refuses the message (nack) or returns it because no queue is bound for the routing key.
     */
    private fun send(outgoing: OutgoingMessagesEntity, routingKey: String) {
        val correlation = CorrelationData(outgoing.id.toString())

        try {
            rabbitTemplate.send(properties.exchange, routingKey, toAmqpMessage(outgoing), correlation)
            val confirm = correlation.future.get(properties.confirmTimeout.toMillis(), TimeUnit.MILLISECONDS)

            if (!confirm.isAck) {
                throw OutboxPublishException("broker refused the message (nack): ${confirm.reason}")
            }
            // With mandatory=true and correlated confirms, Spring sets `returned` before completing the future.
            correlation.returned?.let {
                throw OutboxPublishException(
                    "message returned by the broker (${it.replyCode} ${it.replyText}): " +
                        "no queue bound to exchange '${properties.exchange}' with routing key '$routingKey'",
                )
            }
        } catch (e: OutboxPublishException) {
            throw e
        } catch (e: Exception) {
            throw OutboxPublishException("cannot publish to the broker: ${e.message}", e)
        }
    }

    private fun toAmqpMessage(outgoing: OutgoingMessagesEntity): Message =
        MessageBuilder.withBody(outgoing.payload.toByteArray(Charsets.UTF_8))
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .setContentEncoding(Charsets.UTF_8.name())
            // The outbox row id travels as AMQP message-id: the same id on every retry.
            .setMessageId(outgoing.id.toString())
            .setType(outgoing.messageType)
            .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
            .build()
}

class OutboxPublishException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

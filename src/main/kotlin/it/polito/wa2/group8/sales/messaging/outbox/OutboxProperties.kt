package it.polito.wa2.group8.sales.messaging.outbox

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration of the outbox publisher, read from the `sales.outbox.*` properties
 * (see application.yaml).
 *
 * @property exchange       exchange on which every outbox message is published.
 * @property routingKeys    routing key to use for each `message_type` of outgoing_messages,
 *                          e.g. OrderPaid -> order.paid. A type with no entry is not published.
 * @property batchSize      maximum number of messages read and published in one run.
 * @property confirmTimeout how long to wait for the broker to confirm a single message.
 */
@ConfigurationProperties("sales.outbox")
data class OutboxProperties(
    val exchange: String = "orders-events",
    val routingKeys: Map<String, String> = emptyMap(),
    val batchSize: Int = 50,
    val confirmTimeout: Duration = Duration.ofSeconds(5),
)

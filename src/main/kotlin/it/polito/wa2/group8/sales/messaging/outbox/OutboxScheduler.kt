package it.polito.wa2.group8.sales.messaging.outbox

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Runs the outbox publisher periodically.
 *
 * Kept separate from [OutboxPublisher] so that the call goes through the Spring proxy and
 * publishPending() really runs in a transaction. It can be switched off with
 * sales.outbox.scheduler.enabled=false (the tests do it, to call the publisher by hand).
 *
 * fixedDelay: the next run starts poll-interval-ms after the previous one has finished,
 * so two runs of this scheduler never overlap.
 */
@Component
@ConditionalOnProperty(prefix = "sales.outbox.scheduler", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class OutboxScheduler(
    private val outboxPublisher: OutboxPublisher,
) {

    private val logger = LoggerFactory.getLogger(OutboxScheduler::class.java)

    @Scheduled(fixedDelayString = "\${sales.outbox.scheduler.poll-interval-ms:1000}")
    fun publishOutbox() {
        try {
            outboxPublisher.publishPending()
        } catch (e: Exception) {
            // E.g. database unavailable: log and try again at the next run.
            logger.error("Outbox publishing run failed", e)
        }
    }
}

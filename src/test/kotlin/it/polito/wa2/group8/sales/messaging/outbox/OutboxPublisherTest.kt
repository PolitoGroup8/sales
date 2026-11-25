package it.polito.wa2.group8.sales.messaging.outbox

import it.polito.wa2.group8.sales.TestcontainersConfiguration
import it.polito.wa2.group8.sales.db.entities.OutgoingMessagesEntity
import it.polito.wa2.group8.sales.repositorys.OutgoingMessagesRepoI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import tools.jackson.databind.json.JsonMapper
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Integration test of the outbox publisher against a real PostgreSQL and a real RabbitMQ
 * (Testcontainers). The scheduler is disabled (application-test.yaml): each test calls
 * publishPending() itself.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
class OutboxPublisherTest {

    @Autowired private lateinit var publisher: OutboxPublisher
    @Autowired private lateinit var outgoingMessagesRepo: OutgoingMessagesRepoI
    @Autowired private lateinit var rabbitTemplate: RabbitTemplate
    @Autowired private lateinit var amqpAdmin: AmqpAdmin
    @Autowired private lateinit var salesEventsExchange: TopicExchange

    private val jsonMapper = JsonMapper()

    /** Plays the role of the Tickets queue: bound to orders-events with key order.paid. */
    private val consumerQueue = "test.tickets.order-paid"

    @BeforeEach
    fun cleanState() {
        outgoingMessagesRepo.deleteAll()
        amqpAdmin.deleteQueue(consumerQueue)
    }

    @AfterEach
    fun removeQueue() {
        amqpAdmin.deleteQueue(consumerQueue)
    }

    @Test
    fun `publishes a pending message and marks it as published`() {
        bindConsumerQueue()
        val payload = """{"type":"OrderPaid","version":1,"data":{"orderId":"ord-1"}}"""
        val row = saveOutgoing("OrderPaid", payload)

        val published = publisher.publishPending()

        assertEquals(1, published)

        val received = rabbitTemplate.receive(consumerQueue, 5_000)
        assertNotNull(received, "the message must reach the queue bound with order.paid")
        assertEquals(row.id.toString(), received.messageProperties.messageId)
        assertEquals("OrderPaid", received.messageProperties.type)
        assertEquals(jsonMapper.readTree(payload), jsonMapper.readTree(String(received.body, Charsets.UTF_8)))

        assertNotNull(outgoingMessagesRepo.findById(row.id).get().publishedAt)
    }

    @Test
    fun `does not publish again a message already published`() {
        bindConsumerQueue()
        saveOutgoing("OrderPaid", """{"type":"OrderPaid"}""")

        assertEquals(1, publisher.publishPending())
        assertNotNull(rabbitTemplate.receive(consumerQueue, 5_000))

        assertEquals(0, publisher.publishPending())
        assertNull(rabbitTemplate.receive(consumerQueue, 1_000))
    }

    @Test
    fun `keeps the message when no queue is bound for its routing key`() {
        // No bindConsumerQueue(): the broker cannot route order.paid anywhere and returns the message.
        val row = saveOutgoing("OrderPaid", """{"type":"OrderPaid"}""")

        val published = publisher.publishPending()

        assertEquals(0, published)
        assertNull(
            outgoingMessagesRepo.findById(row.id).get().publishedAt,
            "an unroutable message must stay in the outbox instead of being lost",
        )
    }

    @Test
    fun `skips a message type with no routing key and publishes the others`() {
        bindConsumerQueue()
        val unknown = saveOutgoing("SomethingElse", """{"type":"SomethingElse"}""")
        val orderPaid = saveOutgoing("OrderPaid", """{"type":"OrderPaid"}""")

        val published = publisher.publishPending()

        assertEquals(1, published)
        assertNull(outgoingMessagesRepo.findById(unknown.id).get().publishedAt)
        assertNotNull(outgoingMessagesRepo.findById(orderPaid.id).get().publishedAt)
    }

    private fun bindConsumerQueue() {
        // Durable: RabbitMQ 4 refuses transient non-exclusive queues (transient_nonexcl_queues).
        val queue = Queue(consumerQueue, true)
        amqpAdmin.declareQueue(queue)
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(salesEventsExchange).with("order.paid"))
    }

    private fun saveOutgoing(type: String, payload: String): OutgoingMessagesEntity =
        outgoingMessagesRepo.save(
            OutgoingMessagesEntity(id = UUID.randomUUID(), messageType = type, payload = payload),
        )
}

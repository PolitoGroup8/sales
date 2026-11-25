package it.polito.wa2.group8.sales.messaging.outbox

import org.springframework.amqp.core.TopicExchange
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties::class)
class OutboxConfig {

    /**
     * The exchange on which Sales publishes its events (OrderPaid, ...).
     *
     * Declared from code: Spring's RabbitAdmin creates it on the broker at the first
     * connection, if it does not exist yet. Sales owns this exchange as its producer;
     * each consumer (e.g. Tickets) declares its own queue and binds it to the exchange.
     *
     * Topic type: consumers can bind with patterns (order.* , #) as well as exact keys.
     * Durable: it survives a broker restart.
     */
    @Bean
    fun salesEventsExchange(properties: OutboxProperties): TopicExchange =
        TopicExchange(properties.exchange, true, false)
}

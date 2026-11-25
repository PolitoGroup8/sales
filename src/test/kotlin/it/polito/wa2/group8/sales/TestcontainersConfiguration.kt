package it.polito.wa2.group8.sales

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.rabbitmq.RabbitMQContainer

/**
 * Real PostgreSQL and RabbitMQ, started in Docker for the tests (Testcontainers).
 *
 * @ServiceConnection makes Spring Boot connect the DataSource and the RabbitMQ connection
 * factory to these containers, ignoring the SALES_DB_* and RABBITMQ_* variables of .env.
 * The containers are started once and reused by every test class importing this configuration.
 *
 * Requires Docker running, on the developer machine as on the CI runner.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer = PostgreSQLContainer("postgres:17")

    @Bean
    @ServiceConnection
    fun rabbitContainer(): RabbitMQContainer = RabbitMQContainer("rabbitmq:4-management")
}

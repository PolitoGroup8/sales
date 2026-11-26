package it.polito.wa2.group8.sales.messaging.config

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper

@Configuration
class RabbitConfig {

    @Bean
    fun messageConverter(jsonMapper: JsonMapper): MessageConverter =
        JacksonJsonMessageConverter(jsonMapper)
}
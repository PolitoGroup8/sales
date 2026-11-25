package it.polito.wa2.group8.sales.messaging.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PaymentSucceededEventDTO::class, name = "PAYMENT_SUCCESS"),
)
@JsonIgnoreProperties(ignoreUnknown = true)
sealed interface RabbitEventDTO {
    val eventId: String
    val occurredAt: String
    val type: String
    val message: String
}
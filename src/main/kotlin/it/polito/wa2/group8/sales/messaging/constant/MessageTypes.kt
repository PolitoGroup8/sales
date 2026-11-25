package it.polito.wa2.group8.sales.messaging.constant

enum class MessageTypes(val wireValue: String) {
    PAYMENT_SUCCESS("PAYMENT_SUCCESS"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun from(value: String?): MessageTypes {
            return entries.firstOrNull { it.wireValue == value } ?: UNKNOWN
        }
    }
}
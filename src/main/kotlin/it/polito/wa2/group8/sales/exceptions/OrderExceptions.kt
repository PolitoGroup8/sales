package it.polito.wa2.group8.sales.exceptions

import java.util.UUID

/**
 * Thrown when an order referenced by an operation or event does not exist.
 */
class OrderNotFoundException(
    val orderId: UUID,
    val paymentId: UUID? = null
) : RuntimeException(
    if (paymentId != null) {
        "Order not found for orderId=$orderId, paymentId=$paymentId"
    } else {
        "Order not found for orderId=$orderId"
    }
)

/**
 * Thrown when an order is in an inconsistent or unexpected data state.
 */
class OrderDataAnomalyException(
    val orderId: UUID,
    reason: String
) : RuntimeException("Data anomaly for orderId=$orderId: $reason")

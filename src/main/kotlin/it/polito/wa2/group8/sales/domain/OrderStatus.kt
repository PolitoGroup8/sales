package it.polito.wa2.group8.sales.domain

enum class OrderStatus {
    PENDING_PAYMENT, PAID, PAYMENT_FAILED, EXPIRED, CANCELLED;

    companion object {
        fun from(strOrderStatus: String): OrderStatus {
            val normalized = strOrderStatus.trim().uppercase()
            return entries.firstOrNull { it.name == normalized }
                ?: throw IllegalArgumentException("Unsupported order status: '$strOrderStatus'")
        }
    }
}
package it.polito.wa2.group8.sales.domain

enum class SeatStatus {
    FREE, HELD, SOLD;

    companion object {
        fun from(strSeatStatus: String): SeatStatus {
            val normalized = strSeatStatus.trim().uppercase()
            return entries.firstOrNull { it.name == normalized }
                ?: throw IllegalArgumentException("Unsupported seat status: '$strSeatStatus'")
        }
    }
}
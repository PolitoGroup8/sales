package it.polito.wa2.group8.sales.domain

enum class Currency {
    EUR, USD, JPY, GBP;

    companion object {
        fun from(strCurrency: String): Currency {
            val normalized = strCurrency.trim().uppercase()
            return entries.firstOrNull { it.name == normalized }
                ?: throw IllegalArgumentException("Unsupported currency: '$strCurrency'")
        }
    }
}
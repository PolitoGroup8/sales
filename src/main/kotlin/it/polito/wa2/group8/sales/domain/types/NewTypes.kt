package it.polito.wa2.group8.sales.domain.types

import java.math.BigDecimal
import java.util.UUID

/**
 * Generic entity identifier wrapping a UUID.
 * Used across all domain objects in place of raw UUID or type-specific ID newtypes.
 */
@JvmInline
value class EntityId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun of(uuid: UUID): EntityId = EntityId(uuid)
        fun fromString(s: String): EntityId = EntityId(UUID.fromString(s))
        fun random(): EntityId = EntityId(UUID.randomUUID())
    }
}

@JvmInline
value class Amount(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Amount must not be blank"
        }
        val parsed = value.toBigDecimalOrNull()
        require(parsed != null) {
            "Amount must be a valid decimal number: $value"
        }
        require(parsed > BigDecimal.ZERO) {
            "Amount must be greater than zero."
        }
    }

    override fun toString(): String = value
}
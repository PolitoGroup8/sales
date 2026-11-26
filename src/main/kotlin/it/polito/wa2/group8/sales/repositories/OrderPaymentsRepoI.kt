package it.polito.wa2.group8.sales.repositories

import it.polito.wa2.group8.sales.db.entities.OrderPaymentsEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderPaymentsRepoI : JpaRepository<OrderPaymentsEntity, UUID> {

    /**
     * Checks if an external payment reference is already stored.
     * Used for idempotency check: if paymentId exists, the event was already processed.
     */
    fun existsByPaymentsPaymentId(paymentId: UUID): Boolean
}

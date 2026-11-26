package it.polito.wa2.group8.sales.repositories

import it.polito.wa2.group8.sales.db.entities.OrderItemsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface OrderItemsRepoI : JpaRepository<OrderItemsEntity, UUID> {

    /**
     * Loads all order items for an order along with their seats (JOIN FETCH),
     * avoiding N+1 queries and ensuring seats are present in the JPA session.
     */
    @Query("SELECT i FROM OrderItemsEntity i JOIN FETCH i.seat WHERE i.order.id = :orderId")
    fun findAllWithSeatByOrderId(@Param("orderId") orderId: UUID): List<OrderItemsEntity>
}

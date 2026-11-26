package it.polito.wa2.group8.sales.repositories

import it.polito.wa2.group8.sales.db.entities.OrdersEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrdersRepoI : JpaRepository<OrdersEntity, UUID> {
}
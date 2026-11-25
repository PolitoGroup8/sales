package it.polito.wa2.group8.sales.repositorys

import it.polito.wa2.group8.sales.db.entities.OrdersEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface OrdersRepoI : JpaRepository<OrdersEntity, UUID> {
}
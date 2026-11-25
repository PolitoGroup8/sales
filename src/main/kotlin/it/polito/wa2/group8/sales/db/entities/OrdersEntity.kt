package it.polito.wa2.group8.sales.db.entities

import it.polito.wa2.group8.sales.db.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
class OrdersEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "sales_session_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_orders_sales_session")
    )
    var salesSession: SalesSessionEntity,

    @Column(name = "customer_id", nullable = false, updatable = false)
    var customerId: UUID,

    @Column(name = "total_amount", nullable = false, length = 10)
    var totalAmount: String,

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String = "EUR",

    @Column(name = "status", nullable = false, length = 30)
    var status: String = "PENDING_PAYMENT",

    @Column(name = "expires_at", nullable = false, updatable = false)
    var expiresAt: OffsetDateTime,

    @Generated(event = [EventType.INSERT])
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Generated(event = [EventType.INSERT])
    @Column(name = "updated_at", nullable = false, insertable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
): BaseEntity()
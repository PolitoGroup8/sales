package it.polito.wa2.group8.sales.db.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "order_payments",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_order_payments_external",
            columnNames = ["payments_payment_id"]
        )
    ]
)
class OrderPaymentsEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "sales_order_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_order_payments_order")
    )
    var order: OrdersEntity,

    @Column(name = "payments_payment_id", nullable = false, updatable = false)
    var paymentsPaymentId: UUID,

    @Generated(event = [EventType.INSERT])
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)
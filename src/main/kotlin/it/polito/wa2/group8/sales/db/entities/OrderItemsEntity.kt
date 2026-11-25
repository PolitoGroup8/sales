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
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "order_items",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_order_items_seat",
            columnNames = ["sales_order_id", "sales_seat_id"]
        )
    ]
)
class OrderItemsEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "sales_order_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_order_items_order")
    )
    var order: OrdersEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "sales_seat_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_order_items_seat")
    )
    var seat: SessionSeatEntity,

    @Column(name = "sales_seat_label", nullable = false, length = 50, updatable = false)
    var salesSeatLabel: String,

    @Column(name = "sales_unit_price", nullable = false, length = 10, updatable = false)
    var salesUnitPrice: String,

    @Column(name = "sales_currency", nullable = false, length = 3, updatable = false)
    var salesCurrency: String = "EUR"
): BaseEntity()
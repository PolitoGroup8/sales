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
    name = "session_seats",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_session_seats_session_seat",
            columnNames = ["sales_session_id", "catalog_seat_id"]
        )
    ]
)
class SessionSeatEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "sales_session_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_session_seats_sales_session")
    )
    var salesSession: SalesSessionEntity,

    @Column(name = "catalog_seat_id", nullable = false, updatable = false)
    var catalogSeatId: UUID,

    @Column(name = "catalog_seat_label", nullable = false, length = 50)
    var catalogSeatLabel: String,

    @Column(name = "status", nullable = false, length = 10)
    var status: String = "FREE",

    @Column(name = "catalog_price", nullable = false, length = 10)
    var catalogPrice: String,

    @Column(name = "catalog_currency", nullable = false, length = 3)
    var catalogCurrency: String = "EUR"
): BaseEntity()
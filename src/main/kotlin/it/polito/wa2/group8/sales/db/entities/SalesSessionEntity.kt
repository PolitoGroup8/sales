package it.polito.wa2.group8.sales.db.entities

import it.polito.wa2.group8.sales.db.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "sales_sessions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_sales_sessions_catalog_session",
            columnNames = ["catalog_session_id"]
        )
    ]
)
class SalesSessionEntity(

    @Column(name = "catalog_event_id", nullable = false, updatable = false)
    var catalogEventId: UUID,

    @Column(name = "catalog_session_id", nullable = false, updatable = false)
    var catalogSessionId: UUID,

    @Column(name = "catalog_venue_id", nullable = false, updatable = false)
    var catalogVenueId: UUID,

    @Column(name = "catalog_event_name", nullable = false, updatable = false)
    var catalogEventName: String,

    @Column(name = "catalog_session_start_at", nullable = false)
    var catalogSessionStartAt: OffsetDateTime,

    @Column(name = "catalog_sales_start_at", nullable = false)
    var catalogSalesStartAt: OffsetDateTime,

    @Column(name = "catalog_sales_end_at", nullable = false)
    var catalogSalesEndAt: OffsetDateTime,

    @Column(name = "catalog_max_tickets_per_order", nullable = false)
    var catalogMaxTicketsPerOrder: Int,

    @Column(name = "catalog_venue_name", nullable = false, length = 255)
    var catalogVenueName: String,

    @Column(name = "catalog_venue_city", nullable = false, length = 255)
    var catalogVenueCity: String,

    @Column(name = "snapshot_version", nullable = false)
    var snapshotVersion: Long,

    @Column(name = "snapshot_created_at", nullable = false, updatable = false)
    var snapshotCreatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "snapshot_updated_at", nullable = false)
    var snapshotUpdatedAt: OffsetDateTime = OffsetDateTime.now(),
) : BaseEntity()
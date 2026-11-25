package it.polito.wa2.group8.sales.db.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
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
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID,

    @Column(name = "catalog_event_id", nullable = false, updatable = false)
    val catalogEventId: UUID,

    @Column(name = "catalog_session_id", nullable = false, updatable = false)
    val catalogSessionId: UUID,

    @Column(name = "catalog_venue_id", nullable = false, updatable = false)
    val catalogVenueId: UUID,

    @Column(name = "catalog_session_start_at", nullable = false)
    val catalogSessionStartAt: OffsetDateTime,

    @Column(name = "catalog_sales_start_at", nullable = false)
    val catalogSalesStartAt: OffsetDateTime,

    @Column(name = "catalog_sales_end_at", nullable = false)
    val catalogSalesEndAt: OffsetDateTime,

    @Column(name = "catalog_max_tickets_per_order", nullable = false)
    val catalogMaxTicketsPerOrder: Int,

    @Column(name = "snapshot_version", nullable = false)
    var snapshotVersion: Long,

    @Column(name = "snapshot_created_at", nullable = false, updatable = false)
    val snapshotCreatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "snapshot_updated_at", nullable = false)
    var snapshotUpdatedAt: OffsetDateTime = OffsetDateTime.now(),
)
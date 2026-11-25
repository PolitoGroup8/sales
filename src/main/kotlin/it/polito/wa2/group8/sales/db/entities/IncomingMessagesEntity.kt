package it.polito.wa2.group8.sales.db.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.generator.EventType
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "incoming_messages")
class IncomingMessageEntity(
    @Id
    @Column(name = "message_id", nullable = false, updatable = false)
    var messageId: UUID,

    @Column(name = "message_type", nullable = false, length = 100, updatable = false)
    var messageType: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb", updatable = false)
    var payload: String,

    @Generated(event = [EventType.INSERT])
    @Column(name = "processed_at", nullable = false, updatable = false, insertable = false)
    var processedAt: OffsetDateTime = OffsetDateTime.now()
)
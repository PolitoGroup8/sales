package it.polito.wa2.group8.sales.db.entities

import it.polito.wa2.group8.sales.db.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
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

    @Column(name = "external_message_id", nullable = false, unique = true, updatable = false)
    var externalMessageId: UUID,

    @Column(name = "message_type", nullable = false, length = 100, updatable = false)
    var messageType: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb", updatable = false)
    var payload: String,


    @Column(name = "processed_at", nullable = false, updatable = false)
    var processedAt: OffsetDateTime,
): BaseEntity()
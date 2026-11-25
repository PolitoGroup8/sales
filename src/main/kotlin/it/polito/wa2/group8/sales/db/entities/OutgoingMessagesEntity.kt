package it.polito.wa2.group8.sales.db.entities

import it.polito.wa2.group8.sales.db.BaseEntity
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
@Table(name = "outgoing_messages")
class OutgoingMessagesEntity(

    @Column(name = "message_type", nullable = false, length = 100, updatable = false)
    var messageType: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb", updatable = false)
    var payload: String,

    @Generated(event = [EventType.INSERT])
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "published_at", nullable = true)
    var publishedAt: OffsetDateTime? = null
): BaseEntity()
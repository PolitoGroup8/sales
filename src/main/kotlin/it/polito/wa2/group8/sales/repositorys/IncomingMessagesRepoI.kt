package it.polito.wa2.group8.sales.repositorys

import it.polito.wa2.group8.sales.db.entities.IncomingMessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.UUID

interface IncomingMessagesRepoI: JpaRepository<IncomingMessageEntity, UUID> {

    @Modifying
    @Query(
        nativeQuery = true,
        value = """
            INSERT INTO incoming_messages (external_message_id, message_type, payload, processed_at)
            VALUES (:externalMessageId, :messageType, CAST(:payload AS jsonb), :processedAt)
            ON CONFLICT (external_message_id) DO NOTHING
        """
    )
    fun insertIfAbsent(
        externalMessageId: UUID,
        messageType: String,
        payload: String,
        processedAt: OffsetDateTime,
    ): Int
}
package it.polito.wa2.group8.sales.repositorys

import it.polito.wa2.group8.sales.db.entities.OutgoingMessagesEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface OutgoingMessagesRepoI : JpaRepository<OutgoingMessagesEntity, UUID> {

    /**
     * Loads the oldest messages not yet published and locks them (SELECT ... FOR UPDATE).
     *
     * SKIP LOCKED makes concurrent publishers (e.g. two instances of Sales) skip the rows
     * that another publisher is already handling, instead of waiting for them or publishing
     * them twice. The rows stay locked until the calling transaction ends.
     *
     * Must be called inside a transaction. Uses the partial index
     * idx_outgoing_messages_unpublished (created_at WHERE published_at IS NULL).
     */
    @Query(
        value = """
            SELECT *
            FROM outgoing_messages
            WHERE published_at IS NULL
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true,
    )
    fun findUnpublishedForUpdate(@Param("batchSize") batchSize: Int): List<OutgoingMessagesEntity>
}

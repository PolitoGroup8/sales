package it.polito.wa2.group8.sales.repositories

import it.polito.wa2.group8.sales.db.entities.OutgoingMessagesEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OutgoingMessagesRepoI : JpaRepository<OutgoingMessagesEntity, UUID> {

    fun findAllByPublishedAtIsNullOrderByCreatedAtAsc(): List<OutgoingMessagesEntity>
}
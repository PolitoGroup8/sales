package it.polito.wa2.group8.sales.repositories

import it.polito.wa2.group8.sales.db.entities.SessionSeatEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SessionSeatsRepoI : JpaRepository<SessionSeatEntity, UUID>
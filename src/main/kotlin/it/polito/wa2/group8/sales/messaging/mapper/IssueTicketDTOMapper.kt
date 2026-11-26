package it.polito.wa2.group8.sales.messaging.mapper

import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketData
import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketEvent
import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketLine
import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketPayload
import it.polito.wa2.group8.sales.domain.types.Email
import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import it.polito.wa2.group8.sales.messaging.dto.IssueTicketDataDTO
import it.polito.wa2.group8.sales.messaging.dto.IssueTicketEventDTO
import it.polito.wa2.group8.sales.messaging.dto.IssueTicketLineDTO
import it.polito.wa2.group8.sales.messaging.dto.IssueTicketPayloadDTO
import kotlin.time.Instant

fun IssueTicketPayload.toDTO(): IssueTicketPayloadDTO =
    IssueTicketPayloadDTO(
        type = type.name,
        version = version,
        data = data.toDTO()
    )

fun IssueTicketData.toDTO(): IssueTicketDataDTO =
    IssueTicketDataDTO(
        orderId = orderId.value.toString(),
        customerId = customerId.value.toString(),
        customerEmail = customerEmail.value,
        event = event.toDTO(),
        lines = lines.map { it.toDTO() }
    )

fun IssueTicketEvent.toDTO(): IssueTicketEventDTO =
    IssueTicketEventDTO(
        eventId = eventId.value.toString(),
        sessionId = sessionId.value.toString(),
        eventTitle = eventTitle,
        startsAt = startsAt.toString(),
        venueName = venueName,
        venueCity = venueCity
    )

fun IssueTicketLine.toDTO(): IssueTicketLineDTO =
    IssueTicketLineDTO(
        seatId = catalogSeatId.value.toString(),
        seatLabel = catalogSeatLabel,
        sectorId = catalogSectorId.value.toString(),
        sector = sector
    )

fun IssueTicketPayloadDTO.toDomain(): IssueTicketPayload =
    IssueTicketPayload(
        type = MessageTypes.valueOf(type),
        version = version,
        data = data.toDomain()
    )

fun IssueTicketDataDTO.toDomain(): IssueTicketData =
    IssueTicketData(
        orderId = EntityId.fromString(orderId),
        customerId = EntityId.fromString(customerId),
        customerEmail = Email(customerEmail),
        event = event.toDomain(),
        lines = lines.map { it.toDomain() }
    )

fun IssueTicketEventDTO.toDomain(): IssueTicketEvent =
    IssueTicketEvent(
        eventId = EntityId.fromString(eventId),
        sessionId = EntityId.fromString(sessionId),
        eventTitle = eventTitle,
        startsAt = Instant.parse(startsAt),
        venueName = venueName,
        venueCity = venueCity
    )

fun IssueTicketLineDTO.toDomain(): IssueTicketLine =
    IssueTicketLine(
        catalogSeatId = EntityId.fromString(seatId),
        catalogSeatLabel = seatLabel,
        catalogSectorId = EntityId.fromString(sectorId),
        sector = sector
    )
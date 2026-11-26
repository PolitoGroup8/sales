package it.polito.wa2.group8.sales.messaging.dto

/**
 * {
 *   "type": "ORDER_PAID",
 *   "version": 1,
 *   "data": {
 *     "orderId": "66666666-6666-6666-6666-666666666661",
 *     "customerId": "99999999-9999-9999-9999-999999999992",
 *     "customerEmail": "customer@example.test",
 *     "event": {
 *       "eventId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
 *       "sessionId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
 *       "eventTitle": "Concerto",
 *       "startsAt": "2026-12-10T20:00:00Z",
 *       "venueName": "Teatro Regio",
 *       "venueCity": "Torino"
 *     },
 *     "lines": [
 *       {
 *         "seatId": "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3",
 *         "seatLabel": "A3",
 *         "sectorId": "ffffffff-ffff-ffff-ffff-fffffffffff1",
 *         "sector": "Platea"
 *       }
 *     ]
 *   }
 * }
 */

data class IssueTicketPayloadDTO(
    val type: String,
    val version: Int,
    val data: IssueTicketDataDTO
)

data class IssueTicketDataDTO(
    val orderId: String,
    val customerId: String,
    val customerEmail: String,
    val event: IssueTicketEventDTO,
    val lines: List<IssueTicketLineDTO>
)

data class IssueTicketEventDTO(
    val eventId: String,
    val sessionId: String,
    val eventTitle: String,
    val startsAt: String,
    val venueName: String,
    val venueCity: String
)

data class IssueTicketLineDTO(
    val seatId: String,
    val seatLabel: String,
    val sectorId: String,
    val sector: String
)
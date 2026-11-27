package it.polito.wa2.group8.sales.services.impl

import it.polito.wa2.group8.sales.db.entities.OrderItemsEntity
import it.polito.wa2.group8.sales.db.entities.OrderPaymentsEntity
import it.polito.wa2.group8.sales.db.entities.OrdersEntity
import it.polito.wa2.group8.sales.domain.OrderStatus
import it.polito.wa2.group8.sales.domain.SeatStatus
import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketData
import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketEvent
import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketLine
import it.polito.wa2.group8.sales.domain.messages.outgoing.IssueTicketPayload
import it.polito.wa2.group8.sales.domain.types.Email
import it.polito.wa2.group8.sales.domain.types.EntityId
import it.polito.wa2.group8.sales.exceptions.OrderDataAnomalyException
import it.polito.wa2.group8.sales.exceptions.OrderNotFoundException
import it.polito.wa2.group8.sales.messaging.constant.MessageTypes
import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceeded
import it.polito.wa2.group8.sales.repositories.OrderItemsRepoI
import it.polito.wa2.group8.sales.repositories.OrderPaymentsRepoI
import it.polito.wa2.group8.sales.repositories.OrdersRepoI
import it.polito.wa2.group8.sales.repositories.SessionSeatsRepoI
import it.polito.wa2.group8.sales.services.OrderServiceI
import it.polito.wa2.group8.sales.services.OutgoingMessagesServiceI

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.time.toKotlinInstant

@Service
class OrderService(
    private val ordersRepo: OrdersRepoI,
    private val sessionSeatsRepo: SessionSeatsRepoI,
    private val orderItemsRepo: OrderItemsRepoI,
    private val orderPaymentsRepo: OrderPaymentsRepoI,
    private val outgoingMessagesService: OutgoingMessagesServiceI
) : OrderServiceI {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    @Transactional(rollbackFor = [Exception::class])
    override fun holdPaymentSucceeded(paymentSucceeded: PaymentSucceeded) {
        val payloadOrderId = paymentSucceeded.orderId.value
        val payloadPaymentId = paymentSucceeded.paymentId.value

        logger.debug(
            "Processing payment: orderId={}, paymentId={}",
            payloadOrderId,
            payloadPaymentId
        )

        val orderEntity = ordersRepo
            .findById(payloadOrderId)
            .orElse(null)

        if (orderEntity == null) {
            logger.error(
                "Order not found: orderId={}, paymentId={}",
                payloadOrderId,
                payloadPaymentId
            )
            throw OrderNotFoundException(payloadOrderId, payloadPaymentId)
        }
        else {
            val orderEntityId = orderEntity.id!!
            when (val orderEntityStatus = orderEntity.status) {

                OrderStatus.PENDING_PAYMENT.name -> {
                    logger.debug("Order is PENDING_PAYMENT, confirming reservation for orderId={}", orderEntityId)

                    val orderItemsWithSeats  = orderItemsRepo.findAllWithSeatByOrderId(orderEntityId)

                    runChecks(orderEntityId, orderItemsWithSeats)

                    confirmPayment(
                        orderEntity = orderEntity,
                        orderItemsWithSeats = orderItemsWithSeats ,
                        paymentId = payloadPaymentId
                    )

                    logger.info(
                        "Order successfully paid. orderId={}, paymentId={}, seatsSold={}",
                        orderEntityId,
                        payloadPaymentId,
                        orderItemsWithSeats.size
                    )
                }

                OrderStatus.PAID.name -> {
                    logger.warn(
                        "Order already paid: orderId={}, paymentId={}",
                        orderEntityId,
                        payloadPaymentId
                    )
                }

                OrderStatus.CANCELLED.name -> {
                    logger.warn(
                        "Payment received for cancelled order: orderId={}, paymentId={}",
                        orderEntityId,
                        payloadPaymentId
                    )
                }

                OrderStatus.EXPIRED.name -> {
                    logger.warn(
                        "Payment received for expired order: orderId={}, paymentId={}",
                        orderEntityId,
                        payloadPaymentId
                    )
                }

                OrderStatus.PAYMENT_FAILED.name -> {
                    logger.warn(
                        "Payment received for order with failed payment: orderId={}, paymentId={}",
                        orderEntityId,
                        payloadPaymentId
                    )
                }

                else -> {
                    throw OrderDataAnomalyException(
                        orderEntityId,
                        "Unknown order status '$orderEntityStatus' for paymentId=$payloadPaymentId"
                    )
                }
            }
        }
    }


    private fun runChecks(orderEntityId: UUID, items: List<OrderItemsEntity>) {
        if (items.isEmpty()) {
            throw OrderDataAnomalyException(
                orderEntityId,
                "Order in PENDING_PAYMENT has no order_items"
            )
        }

        val nonHeld = items.filter { it.seat.status != SeatStatus.HELD.name }
        if (nonHeld.isNotEmpty()) {
            throw OrderDataAnomalyException(
                orderEntityId,
                "Seats not in HELD status: ${nonHeld.map { it.seat.id }}"
            )
        }
    }

    private fun confirmPayment(
        orderEntity: OrdersEntity,
        orderItemsWithSeats: List<OrderItemsEntity>,
        paymentId: UUID
    ) {
        val now = OffsetDateTime.now()

        orderEntity.status = OrderStatus.PAID.name
        orderEntity.updatedAt = now

        //STEP 1: put order in PAID
        ordersRepo.save(orderEntity)

        orderItemsWithSeats.forEach {
            it.seat.status = SeatStatus.SOLD.name
        }

        val soldItems = orderItemsWithSeats.map {it.seat}

        //STEP 2: put all seats to SOLD
        sessionSeatsRepo.saveAll(
            soldItems
        )

        //STEP 3: link payment id with sold order
        orderPaymentsRepo.save(
            OrderPaymentsEntity(
                order = orderEntity,
                paymentsPaymentId = paymentId,
                createdAt = now,
            )
        )

        //STEP 4: populate outbox table with domain event payload
        val issueTicketPayload = buildIssueTicketPayload(orderEntity, orderItemsWithSeats)
        outgoingMessagesService.recordOrderPaid(
            payload = issueTicketPayload,
            occurredAt = now
        )
    }

    private fun buildIssueTicketPayload(
        orderEntity: OrdersEntity,
        orderItemsWithSeats: List<OrderItemsEntity>
    ): IssueTicketPayload {
        val sessionEntity = orderEntity.salesSession
        val customerEmail = fakeLookupCustomerEmail(orderEntity.customerId)

        return IssueTicketPayload(
            type = MessageTypes.ORDER_PAID,
            version = 1,
            data = IssueTicketData(
                orderId = EntityId(orderEntity.id!!),
                customerId = EntityId(orderEntity.customerId),
                customerEmail = Email(customerEmail),
                event = IssueTicketEvent(
                    eventId = EntityId(sessionEntity.catalogEventId),
                    sessionId = EntityId(sessionEntity.catalogSessionId),
                    eventTitle = sessionEntity.catalogEventName,
                    startsAt = sessionEntity.catalogSessionStartAt.toInstant().toKotlinInstant(),
                    venueName = sessionEntity.catalogVenueName,
                    venueCity = sessionEntity.catalogVenueCity
                ),
                lines = orderItemsWithSeats.map { item ->
                    val seatEntity = item.seat
                    IssueTicketLine(
                        catalogSeatId = EntityId(seatEntity.catalogSeatId),
                        catalogSeatLabel = item.salesSeatLabel,
                        catalogSectorId = EntityId(seatEntity.catalogSectorId),
                        sector = seatEntity.catalogSectorName
                    )
                }
            )
        )
    }

    private fun fakeLookupCustomerEmail(customerId: UUID): String =
        "user-${customerId}@example.com"
}

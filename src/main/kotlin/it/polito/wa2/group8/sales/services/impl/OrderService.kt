package it.polito.wa2.group8.sales.services.impl

import it.polito.wa2.group8.sales.db.entities.OrderItemsEntity
import it.polito.wa2.group8.sales.db.entities.OrderPaymentsEntity
import it.polito.wa2.group8.sales.db.entities.OrdersEntity
import it.polito.wa2.group8.sales.domain.OrderStatus
import it.polito.wa2.group8.sales.domain.SeatStatus
import it.polito.wa2.group8.sales.messaging.domain.PaymentSucceeded
import it.polito.wa2.group8.sales.repositorys.OrderItemsRepoI
import it.polito.wa2.group8.sales.repositorys.OrderPaymentsRepoI
import it.polito.wa2.group8.sales.repositorys.OrdersRepoI
import it.polito.wa2.group8.sales.repositorys.SessionSeatsRepoI
import it.polito.wa2.group8.sales.services.OrderServiceI
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class OrderService(
    private val ordersRepo: OrdersRepoI,
    private val sessionSeatsRepo: SessionSeatsRepoI,
    private val orderItemsRepo: OrderItemsRepoI,
    private val orderPaymentsRepo: OrderPaymentsRepoI,
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
            throw IllegalStateException("Order not found for orderId=$payloadOrderId, paymentId=$payloadPaymentId")
        }
        else {
            val orderEntityId = orderEntity.id!!
            when (val orderEntityStatus = orderEntity.status) {

                OrderStatus.PENDING_PAYMENT.name -> {
                    logger.debug("Order is PENDING_PAYMENT, confirming reservation for orderId={}", orderEntityId)

                    val orderItemsWithSeats  = orderItemsRepo.findAllWithSeatByOrderId(orderEntityId)

                    if(runChecks(orderEntityId, orderItemsWithSeats )){

                        confirmPayment(
                            orderEntity = orderEntity,
                            items = orderItemsWithSeats ,
                            paymentId = payloadPaymentId
                        )

                        logger.info(
                            "Order successfully paid. orderId={}, paymentId={}, seatsSold={}",
                            orderEntityId,
                            payloadPaymentId,
                            orderItemsWithSeats .size
                        )
                    }
                    else {
                        logger.error(
                            "Aborting payment confirmation due to failed order checks. orderId={}, paymentId={}",
                            orderEntityId,
                            payloadPaymentId
                        )
                        throw IllegalStateException("Failed order checks for orderId=$orderEntityId, paymentId=$payloadPaymentId")
                    }
                }

                OrderStatus.PAID.name -> {
                    logger.warn(
                        "Order is already PAID. Ensuring payment record exists: orderId={}, paymentId={}",
                        orderEntityId,
                        payloadPaymentId
                    )
                }

                OrderStatus.EXPIRED.name,
                OrderStatus.CANCELLED.name,
                OrderStatus.PAYMENT_FAILED.name -> {
                    logger.error(
                        "Payment received for order in terminal status '{}'. No action taken. orderId={}, paymentId={}",
                        orderEntityStatus,
                        orderEntityId,
                        payloadPaymentId
                    )
                }

                else -> {
                    logger.error(
                        "Unknown order status '{}'. orderId={}, paymentId={}",
                        orderEntityStatus,
                        orderEntityId,
                        payloadPaymentId
                    )
                }
            }
        }
    }


    private fun runChecks(orderEntityId: UUID, items: List<OrderItemsEntity>): Boolean {
        var returnValue = true

        if (items.isEmpty()) {
            logger.error(
                "Order in PENDING_PAYMENT has no order_items — data anomaly. orderId={}",
                orderEntityId
            )
            returnValue = false
        }

        val nonHeld = items.filter { it.seat.status != SeatStatus.HELD.name }

        if (nonHeld.isNotEmpty()) {
            logger.error(
                "Seats not in HELD status for order in PENDING_PAYMENT — data anomaly. orderId={}, seatIds={}",
                orderEntityId,
                nonHeld.map { it.seat.id }
            )
            returnValue = false
        }

        return returnValue
    }

    private fun confirmPayment(
        orderEntity: OrdersEntity,
        items: List<OrderItemsEntity>,
        paymentId: UUID
    ) {
        orderEntity.status = OrderStatus.PAID.name
        orderEntity.updatedAt = OffsetDateTime.now()
        ordersRepo.save(orderEntity)

        items.forEach {
            it.seat.status = SeatStatus.SOLD.name
        }
        sessionSeatsRepo.saveAll(items.map { it.seat })

        orderPaymentsRepo.save(
            OrderPaymentsEntity(
                order = orderEntity,
                paymentsPaymentId = paymentId,
                createdAt = OffsetDateTime.now(),
            )
        )
    }
}
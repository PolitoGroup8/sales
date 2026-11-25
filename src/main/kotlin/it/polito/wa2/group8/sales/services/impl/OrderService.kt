package it.polito.wa2.group8.sales.services.impl

import it.polito.wa2.group8.sales.db.entities.OrderPaymentsEntity
import it.polito.wa2.group8.sales.db.entities.OrdersEntity
import it.polito.wa2.group8.sales.domain.OrderNotFoundException
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
    override fun holdPaymentSucceded(paymentSucceeded: PaymentSucceeded) {
        val orderId   = paymentSucceeded.orderId.value
        val paymentId = paymentSucceeded.paymentId.value

        println("[OrderService] Processing payment: orderId=$orderId, paymentId=$paymentId")

        val order = ordersRepo.findByIdWithLock(orderId)
            .orElseThrow {
                println("[OrderService] ERROR: Order not found: orderId=$orderId")
                OrderNotFoundException(paymentSucceeded.orderId, paymentSucceeded.paymentId)
            }
        if (orderPaymentsRepo.existsByPaymentsPaymentId(paymentId)) {
            val msg = "Event already processed (payment already recorded). orderId=$orderId, paymentId=$paymentId"
            println("[OrderService] INFO: $msg")
            logger.info("{}", msg)
            return
        }

        when (order.status) {

            OrderStatus.PENDING_PAYMENT.name -> {
                println("[OrderService] Order is PENDING_PAYMENT, confirming reservation for orderId=$orderId")

                val items = orderItemsRepo.findAllWithSeatByOrderId(orderId)
                if (items.isEmpty()) {
                    val errMsg = "Order in PENDING_PAYMENT has no order_items — data anomaly. orderId=$orderId"
                    println("[OrderService] ERROR: $errMsg")
                    logger.error("{}", errMsg)
                    return
                }

                val nonHeld = items.filter { it.seat.status != SeatStatus.HELD.name }
                if (nonHeld.isNotEmpty()) {
                    val errMsg = "Seats not in HELD status for order in PENDING_PAYMENT — data anomaly. orderId=$orderId, seatIds=${nonHeld.map { it.seat.id }}"
                    println("[OrderService] ERROR: $errMsg")
                    logger.error("{}", errMsg)
                    return
                }

                order.status    = OrderStatus.PAID.name
                order.updatedAt = OffsetDateTime.now()
                ordersRepo.save(order)

                items.forEach { it.seat.status = SeatStatus.SOLD.name }
                sessionSeatsRepo.saveAll(items.map { it.seat })

                recordPayment(order, paymentId)

                val successMsg = "Order successfully paid. orderId=$orderId, paymentId=$paymentId, seatsSold=${items.size}"
                println("[OrderService] SUCCESS: $successMsg")
                logger.info("{}", successMsg)
            }

            OrderStatus.PAID.name -> {
                val warnMsg = "Order is already PAID. Ensuring payment record exists: orderId=$orderId, paymentId=$paymentId"
                println("[OrderService] WARN: $warnMsg")
                logger.warn("{}", warnMsg)
                recordPayment(order, paymentId)
            }

            OrderStatus.EXPIRED.name,
            OrderStatus.CANCELLED.name,
            OrderStatus.PAYMENT_FAILED.name -> {
                val errMsg = "Payment received for order in terminal status '${order.status}'. No action taken. orderId=$orderId, paymentId=$paymentId"
                println("[OrderService] ERROR: $errMsg")
                logger.error("{}", errMsg)
            }

            else -> {
                val errMsg = "Unknown order status '${order.status}'. orderId=$orderId, paymentId=$paymentId"
                println("[OrderService] ERROR: $errMsg")
                logger.error("{}", errMsg)
            }
        }
    }

    private fun recordPayment(order: OrdersEntity, paymentId: UUID) {
        orderPaymentsRepo.save(
            OrderPaymentsEntity(
                id                 = UUID.randomUUID(),
                order              = order,
                paymentsPaymentId  = paymentId,
                createdAt          = OffsetDateTime.now(),
            )
        )
    }
}
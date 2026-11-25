package it.polito.wa2.group8.sales.domain

import it.polito.wa2.group8.sales.domain.types.EntityId

class OrderNotFoundException(orderId: EntityId, paymentId: EntityId? = null) :
    RuntimeException("Order not found during payment processing: orderId=$orderId${if (paymentId != null) ", paymentId=$paymentId" else ""}")

package com.gijun.backend.domain.model.order

import com.gijun.backend.domain.enums.order.OrderStatus
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class Order(
    @Id
    val id: Integer,
    val customerId: Integer,
    val storeId: Integer,
    val addressId: Integer,
    val orderNumber: String,
    val orderStatus: OrderStatus,
    val orderTime: LocalDateTime,
    val acceptedTime: LocalDateTime?,
    val readyTime: LocalDateTime?,
    val deliveredTime: LocalDateTime?,
    val cancelledTime: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deletedAt: LocalDateTime?,
    val totalAmount: Double,
    val discountAmount: Double,
    val deliveryFee: Double,
    val payedAmount: Double,
    val requestStore: String?,
    val requestRider: String?
)

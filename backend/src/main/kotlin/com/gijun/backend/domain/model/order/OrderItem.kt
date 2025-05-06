package com.gijun.backend.domain.model.order

import org.springframework.data.annotation.Id

data class OrderItem(
    @Id
    val id: Long,
    val orderId: Long,
    val menuId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?
)

package com.gijun.backend.domain.model.order

import org.springframework.data.annotation.Id

data class OrderItem(
    @Id
    val id: Integer,
    val orderId: Integer,
    val menuId: Integer,
    val quantity: Integer,
    val unitPrice: Double,
    val totalPrice: Double,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?
)

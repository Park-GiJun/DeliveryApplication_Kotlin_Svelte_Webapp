package com.gijun.backend.domain.model.order

import org.springframework.data.annotation.Id

data class OrderItemOption(
    @Id
    val id: Integer,
    val orderId: Integer,
    val orderItemId: Integer,
    val optionItemId: Integer,
    val name: String,
    val price: Double,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?
)

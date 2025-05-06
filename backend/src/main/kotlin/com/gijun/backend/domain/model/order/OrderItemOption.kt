package com.gijun.backend.domain.model.order

import org.springframework.data.annotation.Id

data class OrderItemOption(
    @Id
    val id: Long,
    val orderId: Long,
    val orderItemId: Long,
    val optionItemId: Long,
    val name: String,
    val price: Double,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?
)

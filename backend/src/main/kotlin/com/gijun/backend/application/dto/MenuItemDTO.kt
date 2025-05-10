package com.gijun.backend.application.dto

data class MenuItemDTO (
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val name: String,
    val description: String?,
    val price: Double,
    val imageUrl: String?,
    val available: Boolean
)
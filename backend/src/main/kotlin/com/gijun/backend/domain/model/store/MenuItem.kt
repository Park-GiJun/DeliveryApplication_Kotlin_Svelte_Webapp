package com.gijun.backend.domain.model.store

import com.gijun.backend.domain.enums.ActivatedStatus
import com.gijun.backend.domain.enums.store.ItemStatus
import org.springframework.data.annotation.Id

data class MenuItem(
    @Id
    val id: Integer,
    val categoryId: Integer,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val itemStatus: ItemStatus,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
    val activatedStatus: ActivatedStatus
)

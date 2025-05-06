package com.gijun.backend.domain.model.store

import com.gijun.backend.domain.enums.ActivatedStatus
import org.springframework.data.annotation.Id

data class MenuCategory(
    @Id
    val id: Long,
    val storeId: Long,
    val displayOrder: Long,
    val name: String,
    val description: String,
    val activatedStatus: ActivatedStatus,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?
)

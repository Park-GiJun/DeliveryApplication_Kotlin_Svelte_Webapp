package com.gijun.backend.domain.model.store

import com.gijun.backend.domain.enums.ActivatedStatus
import org.springframework.data.annotation.Id

data class MenuOption(
    @Id
    val id: Long,
    val menuId: Long,
    val name: String,
    val isRequired: Boolean,
    val displayOrder: Int,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
    val activatedStatus: ActivatedStatus
)

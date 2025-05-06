package com.gijun.backend.domain.model.store

import com.gijun.backend.domain.enums.ActivatedStatus
import org.springframework.data.annotation.Id

data class MenuOption(
    @Id
    val id: Integer,
    val menuId: Integer,
    val name: String,
    val isRequired: Boolean,
    val displayOrder: Integer,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
    val activatedStatus: ActivatedStatus
)

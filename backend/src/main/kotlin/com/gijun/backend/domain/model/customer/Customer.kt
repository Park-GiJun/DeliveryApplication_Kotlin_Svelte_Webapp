package com.gijun.backend.domain.model.customer

import com.gijun.backend.domain.enums.ActivatedStatus
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class Customer(
    @Id
    val id: Long,
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deletedAt: LocalDateTime?,
    val activatedStatus: ActivatedStatus
)

package com.gijun.backend.domain.model.customer

import com.gijun.backend.domain.enums.ActivatedStatus
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class DeliveryAddress(
    @Id
    val id: Integer,
    val customerId: Integer,
    val name: String,
    val address: String,
    val detailAddress: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deletedAt: LocalDateTime?,
    val activatedStatus: ActivatedStatus
)

package com.gijun.backend.domain.model.rider

import com.gijun.backend.domain.enums.ActivatedStatus
import com.gijun.backend.domain.enums.rider.RiderStatus
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class Rider(
    @Id
    val id: Long,
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val riderStatus: RiderStatus,
    val currentLatitude: Double?,
    val currentLongitude: Double?,
    val currentOrderId: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deletedAt: LocalDateTime?,
    val activatedStatus: ActivatedStatus
)
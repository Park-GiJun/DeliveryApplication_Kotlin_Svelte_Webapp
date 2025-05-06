package com.gijun.backend.domain.model.rider

import com.gijun.backend.domain.enums.ActivatedStatus
import com.gijun.backend.domain.enums.rider.RiderStatus
import org.springframework.data.annotation.Id

data class Rider(
    @Id
    val id: Integer,
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val riderStatus: RiderStatus,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
    val activatedStatus: ActivatedStatus
)

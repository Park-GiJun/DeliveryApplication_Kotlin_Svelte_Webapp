package com.gijun.backend.domain.model.simulation

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class RiderLocationHistory(
    @Id
    val id: Long,
    val riderId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: LocalDateTime,
    val heading: Double?,
    val speed: Double?
)
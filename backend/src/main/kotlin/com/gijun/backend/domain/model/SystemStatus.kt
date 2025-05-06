package com.gijun.backend.domain.model

import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.Date

data class SystemStatus(
    @Id
    val id: Long,
    val logDate: Date,
    val logTime: LocalDateTime,
    val webFluxStatus: Boolean,
    val databaseStatus: Boolean,
    val redisStatus: Boolean,
    val kafkaStatus: Boolean
)

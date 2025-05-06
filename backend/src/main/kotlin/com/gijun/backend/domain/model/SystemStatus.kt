package com.gijun.backend.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("system_status")
data class SystemStatus(
    @Id
    val id: Long? = null,
    
    @Column("log_date")
    val logDate: LocalDate,
    
    @Column("log_time")
    val logTime: LocalDateTime = LocalDateTime.now(),
    
    @Column("web_flux_status")
    val webFluxStatus: Boolean = true,
    
    @Column("database_status")
    val databaseStatus: Boolean = true,
    
    @Column("redis_status")
    val redisStatus: Boolean = true,
    
    @Column("kafka_status")
    val kafkaStatus: Boolean = true
)

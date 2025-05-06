package com.gijun.backend.domain.model.simulation

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("rider_location_history")
data class RiderLocationHistory(
    @Id
    val id: Long? = null,
    
    @Column("rider_id")
    val riderId: Long,
    
    @Column("latitude")
    val latitude: Double,
    
    @Column("longitude")
    val longitude: Double,
    
    @Column("timestamp")
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    @Column("heading")
    val heading: Double? = null,
    
    @Column("speed")
    val speed: Double? = null
)

package com.gijun.backend.domain.model.simulation

import com.gijun.backend.domain.enums.simulation.SimulationScenario
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("simulation_config")
data class SimulationConfig(
    @Id
    val id: Long? = null,
    
    @Column("order_generation_interval")
    val orderGenerationInterval: Int = 5, // 주문 생성 간격(초)
    
    @Column("rider_count")
    val riderCount: Int = 10, // 시뮬레이션 라이더 수
    
    @Column("acceptance_delay")
    val acceptanceDelay: Int = 1, // 주문 수락 지연 시간(초)
    
    @Column("min_delivery_time")
    val minDeliveryTime: Int = 3, // 최소 배달 시간(분)
    
    @Column("max_delivery_time")
    val maxDeliveryTime: Int = 8, // 최대 배달 시간(분)
    
    @Column("scenario")
    val scenario: SimulationScenario = SimulationScenario.NORMAL,
    
    @Column("is_active")
    val isActive: Boolean = false,
    
    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

package com.gijun.backend.domain.model.simulation

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class SimulationConfig(
    @Id
    val id: Long,
    val orderGenerationInterval: Int, // 주문 생성 간격(초)
    val riderCount: Int,              // 시뮬레이션 라이더 수
    val acceptanceDelay: Int,         // 주문 수락 지연 시간(초)
    val minDeliveryTime: Int,         // 최소 배달 시간(분)
    val maxDeliveryTime: Int,         // 최대 배달 시간(분)
    val isActive: Boolean,            // 활성화 여부
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
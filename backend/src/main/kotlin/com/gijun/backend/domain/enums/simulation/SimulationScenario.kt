package com.gijun.backend.domain.enums.simulation

enum class SimulationScenario {
    NORMAL,         // 일반적인 상황
    PEAK_TIME,      // 주문량 급증
    BAD_WEATHER,    // 악천후(배달 시간 증가)
    RIDER_SHORTAGE  // 라이더 부족
}
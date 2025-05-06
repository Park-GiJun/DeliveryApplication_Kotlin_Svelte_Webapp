package com.gijun.backend.domain.repository.order

import com.gijun.backend.application.dto.CreateOrderDto
import com.gijun.backend.application.dto.OrderResponseDto
import reactor.core.publisher.Mono

/**
 * 주문 저장소 인터페이스
 */
interface OrderRepository {
    
    /**
     * 새로운 주문을 저장합니다.
     * 
     * @param createOrderDto 주문 생성 요청 DTO
     * @param orderResponseDto 주문 응답 DTO
     * @return 저장된 주문 정보
     */
    fun saveOrder(createOrderDto: CreateOrderDto, orderResponseDto: OrderResponseDto): Mono<OrderResponseDto>
}

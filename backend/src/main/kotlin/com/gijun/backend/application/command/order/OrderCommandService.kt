package com.gijun.backend.application.command.order

import com.gijun.backend.application.dto.CreateOrderDto
import com.gijun.backend.application.dto.OrderResponseDto
import reactor.core.publisher.Mono

/**
 * 주문 커맨드 서비스 인터페이스
 */
interface OrderCommandService {
    
    /**
     * 새로운 주문을 생성합니다.
     *
     * @param createOrderDto 주문 생성 요청 DTO
     * @return 생성된 주문 정보
     */
    fun createOrder(createOrderDto: CreateOrderDto): Mono<OrderResponseDto>
    
    /**
     * 주문을 취소합니다.
     * 
     * @param orderNumber 주문 번호
     * @param reason 취소 사유
     * @return 취소된 주문 정보
     */
    fun cancelOrder(orderNumber: String, reason: String): Mono<OrderResponseDto>
    
    /**
     * 주문 상태를 업데이트합니다.
     * 
     * @param orderNumber 주문 번호
     * @param status 변경할 상태
     * @return 업데이트된 주문 정보
     */
    fun updateOrderStatus(orderNumber: String, status: String): Mono<OrderResponseDto>
}

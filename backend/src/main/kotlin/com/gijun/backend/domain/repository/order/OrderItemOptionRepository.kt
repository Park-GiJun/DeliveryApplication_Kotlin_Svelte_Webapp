package com.gijun.backend.domain.repository.order

import com.gijun.backend.domain.model.order.OrderItemOption
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 주문 항목 옵션 리포지토리 인터페이스
 */
interface OrderItemOptionRepository {
    /**
     * 주문 항목 옵션 저장
     */
    fun save(orderItemOption: OrderItemOption): Mono<OrderItemOption>
    
    /**
     * ID로 주문 항목 옵션 조회
     */
    fun findById(id: Integer): Mono<OrderItemOption>
    
    /**
     * 주문 ID로 주문 항목 옵션 목록 조회
     */
    fun findByOrderId(orderId: Integer): Flux<OrderItemOption>
    
    /**
     * 주문 항목 ID로 주문 항목 옵션 목록 조회
     */
    fun findByOrderItemId(orderItemId: Integer): Flux<OrderItemOption>
    
    /**
     * 옵션 항목 ID로 주문 항목 옵션 목록 조회
     */
    fun findByOptionItemId(optionItemId: Integer): Flux<OrderItemOption>
}

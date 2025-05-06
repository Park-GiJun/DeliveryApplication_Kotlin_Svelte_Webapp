package com.gijun.backend.domain.repository.order

import com.gijun.backend.domain.model.order.OrderItem
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 주문 항목 리포지토리 인터페이스
 */
interface OrderItemRepository {
    /**
     * 주문 항목 저장
     */
    fun save(orderItem: OrderItem): Mono<OrderItem>
    
    /**
     * ID로 주문 항목 조회
     */
    fun findById(id: Integer): Mono<OrderItem>
    
    /**
     * 주문 ID로 주문 항목 목록 조회
     */
    fun findByOrderId(orderId: Integer): Flux<OrderItem>
    
    /**
     * 메뉴 ID로 주문 항목 목록 조회
     */
    fun findByMenuId(menuId: Integer): Flux<OrderItem>
}

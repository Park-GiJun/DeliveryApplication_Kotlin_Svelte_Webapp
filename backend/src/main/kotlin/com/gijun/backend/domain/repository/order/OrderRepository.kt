package com.gijun.backend.domain.repository.order

import com.gijun.backend.domain.model.order.Order
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 주문 리포지토리 인터페이스
 */
interface OrderRepository {
    /**
     * 주문 저장
     */
    fun save(order: Order): Mono<Order>
    
    /**
     * ID로 주문 조회
     */
    fun findById(id: Integer): Mono<Order>
    
    /**
     * 주문 번호로 주문 조회
     */
    fun findByOrderNumber(orderNumber: String): Mono<Order>
    
    /**
     * 고객 ID로 주문 목록 조회 (페이징, 생성일 기준 내림차순)
     */
    fun findByCustomerIdOrderByCreatedAtDesc(customerId: Int, pageable: Pageable): Flux<Order>
    
    /**
     * 매장 ID로 주문 목록 조회 (페이징, 생성일 기준 내림차순)
     */
    fun findByStoreIdOrderByCreatedAtDesc(storeId: Int, pageable: Pageable): Flux<Order>
    
    /**
     * 주문 상태로 주문 목록 조회
     */
    fun findByOrderStatus(status: com.gijun.backend.domain.enums.order.OrderStatus): Flux<Order>
    
    /**
     * 특정 시간 이후 생성된 주문 목록 조회
     */
    fun findByOrderTimeAfter(time: java.time.LocalDateTime): Flux<Order>
}

package com.gijun.backend.application.command.order

import com.gijun.backend.domain.model.order.Order
import reactor.core.publisher.Mono

/**
 * 주문 커맨드 서비스 인터페이스 - 주문 상태를 변경하는 명령을 처리
 */
interface OrderCommandService {
    /**
     * 새 주문 생성
     */
    fun createOrder(command: CreateOrderCommand): Mono<Order>
    
    /**
     * 주문 취소
     */
    fun cancelOrder(orderNumber: String, reason: String): Mono<Order>
    
    /**
     * 주문 상태 업데이트
     */
    fun updateOrderStatus(orderNumber: String, status: String): Mono<Order>
}

/**
 * 주문 생성 커맨드
 */
data class CreateOrderCommand(
    val customerId: Long,
    val storeId: Long,
    val addressId: Long,
    val items: List<OrderItemCommand>,
    val totalAmount: Double,
    val deliveryFee: Double,
    val requestStore: String?,
    val requestRider: String?
) {
    /**
     * 주문 항목 커맨드
     */
    data class OrderItemCommand(
        val menuId: Long,
        val quantity: Int,
        val unitPrice: Double,
        val options: List<OrderItemOptionCommand>
    )
    
    /**
     * 주문 항목 옵션 커맨드
     */
    data class OrderItemOptionCommand(
        val optionItemId: Long,
        val name: String,
        val price: Double
    )
}

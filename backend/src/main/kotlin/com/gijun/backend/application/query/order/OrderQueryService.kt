package com.gijun.backend.application.query.order

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 주문 쿼리 서비스 인터페이스 - 주문 정보 조회
 */
interface OrderQueryService {
    /**
     * 주문 번호로 주문 상세 정보 조회
     */
    fun getOrderByNumber(orderNumber: String): Mono<OrderDetailDTO>
    
    /**
     * 고객 ID로 주문 목록 조회
     */
    fun getOrdersByCustomerId(customerId: Long, page: Int, size: Int): Flux<OrderSummaryDTO>
    
    /**
     * 매장 ID로 주문 목록 조회
     */
    fun getOrdersByStoreId(storeId: Long, page: Int, size: Int): Flux<OrderSummaryDTO>
    
    /**
     * 라이더 ID로 주문 목록 조회
     */
    fun getOrdersByRiderId(riderId: Long, page: Int, size: Int): Flux<OrderSummaryDTO>
}

/**
 * 주문 상세 정보 DTO
 */
data class OrderDetailDTO(
    val orderId: Long,
    val orderNumber: String,
    val storeId: Long,
    val storeName: String?,
    val customerId: Long,
    val customerName: String?,
    val status: String,
    val orderTime: String,
    val acceptedTime: String?,
    val readyTime: String?,
    val deliveredTime: String?,
    val cancelledTime: String?,
    val items: List<OrderItemDTO>,
    val totalAmount: Double,
    val deliveryFee: Double,
    val payedAmount: Double,
    val discountAmount: Double,
    val requestStore: String?,
    val requestRider: String?,
    val riderId: Long?,
    val riderName: String?,
    val estimatedDeliveryTime: String?
)

/**
 * 주문 항목 DTO
 */
data class OrderItemDTO(
    val itemId: Long,
    val menuId: Long,
    val menuName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val options: List<OrderItemOptionDTO>
)

/**
 * 주문 항목 옵션 DTO
 */
data class OrderItemOptionDTO(
    val optionId: Long,
    val optionItemId: Long,
    val name: String,
    val price: Double
)

/**
 * 주문 요약 DTO (목록 조회용)
 */
data class OrderSummaryDTO(
    val orderId: Long,
    val orderNumber: String,
    val storeId: Long,
    val storeName: String?,
    val customerId: Long,
    val customerName: String?,
    val status: String,
    val orderTime: String,
    val totalAmount: Double,
    val deliveryFee: Double,
    val itemCount: Int
)

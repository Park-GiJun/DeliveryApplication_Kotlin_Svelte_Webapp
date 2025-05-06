package com.gijun.backend.domain.dto.order

import com.gijun.backend.application.query.order.OrderDetailDTO
import com.gijun.backend.application.query.order.OrderSummaryDTO

/**
 * 주문 요청 관련 DTO 클래스들
 */

/**
 * 주문 생성 요청 DTO
 */
data class OrderRequestDTO(
    val customerId: Long,
    val storeId: Long,
    val addressId: Long,
    val items: List<OrderItemRequestDTO>,
    val totalAmount: Double,
    val deliveryFee: Double,
    val requestStore: String?,
    val requestRider: String?
)

/**
 * 주문 항목 요청 DTO
 */
data class OrderItemRequestDTO(
    val menuId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val options: List<OrderItemOptionRequestDTO>?
)

/**
 * 주문 항목 옵션 요청 DTO
 */
data class OrderItemOptionRequestDTO(
    val optionItemId: Long,
    val name: String,
    val price: Double
)

/**
 * 주문 취소 요청 DTO
 */
data class OrderCancelRequestDTO(
    val reason: String
)

/**
 * 주문 응답 DTO
 */
data class OrderResponseDTO(
    val success: Boolean,
    val orderId: Long?,
    val orderNumber: String?,
    val message: String
)

/**
 * 주문 상세 응답 DTO
 */
data class OrderDetailResponseDTO(
    val success: Boolean = true,
    val orderId: Long? = null,
    val orderNumber: String? = null,
    val storeId: Long? = null,
    val storeName: String? = null,
    val customerId: Long? = null,
    val customerName: String? = null,
    val status: String? = null,
    val orderTime: String? = null,
    val items: List<OrderItemResponseDTO> = emptyList(),
    val totalAmount: Double? = null,
    val deliveryFee: Double? = null,
    val payedAmount: Double? = null,
    val requestStore: String? = null,
    val requestRider: String? = null,
    val riderId: Long? = null,
    val riderName: String? = null,
    val estimatedDeliveryTime: String? = null,
    val message: String = ""
) {
    companion object {
        fun fromOrderDetail(orderDetail: OrderDetailDTO): OrderDetailResponseDTO {
            return OrderDetailResponseDTO(
                success = true,
                orderId = orderDetail.orderId,
                orderNumber = orderDetail.orderNumber,
                storeId = orderDetail.storeId,
                storeName = orderDetail.storeName,
                customerId = orderDetail.customerId,
                customerName = orderDetail.customerName,
                status = orderDetail.status,
                orderTime = orderDetail.orderTime,
                items = orderDetail.items.map { item ->
                    OrderItemResponseDTO(
                        menuId = item.menuId,
                        menuName = item.menuName,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        totalPrice = item.totalPrice,
                        options = item.options.map { option ->
                            OrderItemOptionResponseDTO(
                                optionItemId = option.optionItemId,
                                name = option.name,
                                price = option.price
                            )
                        }
                    )
                },
                totalAmount = orderDetail.totalAmount,
                deliveryFee = orderDetail.deliveryFee,
                payedAmount = orderDetail.payedAmount,
                requestStore = orderDetail.requestStore,
                requestRider = orderDetail.requestRider,
                riderId = orderDetail.riderId,
                riderName = orderDetail.riderName,
                estimatedDeliveryTime = orderDetail.estimatedDeliveryTime,
                message = "주문 조회 성공"
            )
        }
    }
}

/**
 * 주문 항목 응답 DTO
 */
data class OrderItemResponseDTO(
    val menuId: Long,
    val menuName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val options: List<OrderItemOptionResponseDTO>
)

/**
 * 주문 항목 옵션 응답 DTO
 */
data class OrderItemOptionResponseDTO(
    val optionItemId: Long,
    val name: String,
    val price: Double
)

/**
 * 주문 목록 응답 DTO
 */
data class OrderListResponseDTO(
    val success: Boolean,
    val orders: List<OrderSummaryResponseDTO>,
    val message: String
)

/**
 * 주문 요약 응답 DTO (목록 조회용)
 */
data class OrderSummaryResponseDTO(
    val orderId: Long,
    val orderNumber: String,
    val storeId: Long,
    val storeName: String?,
    val status: String,
    val orderTime: String,
    val totalAmount: Double,
    val deliveryFee: Double
) {
    companion object {
        fun fromOrderSummary(orderSummary: OrderSummaryDTO): OrderSummaryResponseDTO {
            return OrderSummaryResponseDTO(
                orderId = orderSummary.orderId,
                orderNumber = orderSummary.orderNumber,
                storeId = orderSummary.storeId,
                storeName = orderSummary.storeName,
                status = orderSummary.status,
                orderTime = orderSummary.orderTime,
                totalAmount = orderSummary.totalAmount,
                deliveryFee = orderSummary.deliveryFee
            )
        }
    }
}

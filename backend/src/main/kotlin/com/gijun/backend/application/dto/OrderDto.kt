package com.gijun.backend.application.dto

import com.gijun.backend.domain.enums.order.OrderStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "주문 생성 요청 DTO")
data class CreateOrderDto(
    @Schema(description = "고객 ID", example = "1")
    val customerId: Int,
    
    @Schema(description = "매장 ID", example = "1")
    val storeId: Int,
    
    @Schema(description = "배송 주소 ID", example = "1")
    val addressId: Int,
    
    @Schema(description = "주문 상품 목록")
    val items: List<OrderItemDto>,
    
    @Schema(description = "매장 요청사항", example = "매운맛으로 부탁드립니다")
    val requestStore: String? = null,
    
    @Schema(description = "라이더 요청사항", example = "문 앞에 놓아주세요")
    val requestRider: String? = null
)

@Schema(description = "주문 상품 DTO")
data class OrderItemDto(
    @Schema(description = "메뉴 ID", example = "1")
    val menuId: Int,
    
    @Schema(description = "수량", example = "2")
    val quantity: Int,
    
    @Schema(description = "단가", example = "36000")
    val unitPrice: Double? = null,
    
    @Schema(description = "옵션 목록")
    val options: List<OrderItemOptionDto>? = null
)

@Schema(description = "주문 상품 옵션 DTO")
data class OrderItemOptionDto(
    @Schema(description = "옵션 ID", example = "1")
    val optionItemId: Int,
    
    @Schema(description = "옵션명", example = "소스 선택")
    val name: String? = null,
    
    @Schema(description = "옵션 가격", example = "500")
    val price: Double? = null
)

@Schema(description = "주문 응답 DTO")
data class OrderResponseDto(
    @Schema(description = "주문 ID", example = "ORD20250506001")
    val id: String,
    
    @Schema(description = "주문 번호", example = "A20250506-001")
    val orderNumber: String,
    
    @Schema(description = "주문 상태", example = "CREATED")
    val status: OrderStatus,
    
    @Schema(description = "주문 시간")
    val orderTime: LocalDateTime,
    
    @Schema(description = "총 상품 금액", example = "36500")
    val totalAmount: Double,
    
    @Schema(description = "배달료", example = "3000")
    val deliveryFee: Double,
    
    @Schema(description = "할인 금액", example = "2000")
    val discountAmount: Double,
    
    @Schema(description = "최종 결제 금액", example = "38500")
    val payedAmount: Double,
    
    @Schema(description = "예상 배달 시간(분)", example = "30")
    val estimatedDeliveryMinutes: Int? = null
)

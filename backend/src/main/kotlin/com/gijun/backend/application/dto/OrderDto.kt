package com.gijun.backend.application.dto

import com.gijun.backend.domain.enums.order.OrderStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 주문 생성 요청 DTO
 */
@Schema(description = "주문 생성 요청 DTO")
data class CreateOrderDto(
    @Schema(description = "고객 ID", example = "1", required = true)
    val customerId: Long,
    
    @Schema(description = "매장 ID", example = "1", required = true)
    val storeId: Long,
    
    @Schema(description = "배송 주소 ID", example = "1", required = true)
    val addressId: Long,
    
    @Schema(description = "주문 상품 목록", required = true)
    val items: List<OrderItemDto>,
    
    @Schema(description = "매장 요청사항", example = "양념 소스 많이 주세요", required = false)
    val requestStore: String? = null,
    
    @Schema(description = "라이더 요청사항", example = "문 앞에 놓아주세요", required = false)
    val requestRider: String? = null
) {
    @Schema(description = "주문 생성 요청 예시")
    fun example() = CreateOrderDto(
        customerId = 1,
        storeId = 1,
        addressId = 1,
        items = listOf(
            OrderItemDto(
                menuId = 1,
                quantity = 1,
                unitPrice = 18000.0,
                options = listOf(
                    OrderItemOptionDto(
                        optionItemId = 1,
                        name = "소스 선택",
                        price = 0.0
                    )
                )
            ),
            OrderItemDto(
                menuId = 2,
                quantity = 1,
                unitPrice = 19000.0,
                options = null
            )
        ),
        requestStore = "양념 소스 많이 주세요",
        requestRider = "문 앞에 놓아주세요"
    )
}

/**
 * 주문 상품 DTO
 */
@Schema(description = "주문 상품 DTO")
data class OrderItemDto(
    @Schema(description = "메뉴 ID", example = "1", required = true)
    val menuId: Long,
    
    @Schema(description = "수량", example = "1", required = true)
    val quantity: Int,
    
    @Schema(description = "단가", example = "18000", required = true)
    val unitPrice: Double,
    
    @Schema(description = "옵션 목록", required = false)
    val options: List<OrderItemOptionDto>? = null
)

/**
 * 주문 상품 옵션 DTO
 */
@Schema(description = "주문 상품 옵션 DTO")
data class OrderItemOptionDto(
    @Schema(description = "옵션 ID", example = "1", required = true)
    val optionItemId: Long,
    
    @Schema(description = "옵션명", example = "소스 선택", required = true)
    val name: String,
    
    @Schema(description = "옵션 가격", example = "0", required = true)
    val price: Double
)

/**
 * 주문 응답 DTO
 */
@Schema(description = "주문 응답 DTO")
data class OrderResponseDto(
    @Schema(description = "주문 ID", example = "1", required = true)
    val id: Long,
    
    @Schema(description = "주문 번호", example = "ORD-20250506-001", required = true)
    val orderNumber: String,
    
    @Schema(description = "주문 상태", example = "CREATED", required = true)
    val status: OrderStatus = OrderStatus.CREATED,
    
    @Schema(description = "주문 시간", required = true)
    val orderTime: LocalDateTime = LocalDateTime.now(),
    
    @Schema(description = "총 상품 금액", example = "37000", required = true)
    val totalAmount: Double,
    
    @Schema(description = "배달료", example = "3000", required = true)
    val deliveryFee: Double,
    
    @Schema(description = "할인 금액", example = "0", required = true)
    val discountAmount: Double = 0.0,
    
    @Schema(description = "최종 결제 금액", example = "40000", required = true)
    val payedAmount: Double,
    
    @Schema(description = "예상 배달 소요시간(분)", example = "30", required = false)
    val estimatedDeliveryMinutes: Int? = null,
    
    @Schema(description = "주문 상세 정보", required = false)
    val orderDetails: OrderDetailsDto? = null
) {
    @Schema(description = "주문 응답 예시")
    fun example() = OrderResponseDto(
        id = 1,
        orderNumber = "ORD-20250506-001",
        status = OrderStatus.CREATED,
        orderTime = LocalDateTime.now(),
        totalAmount = 37000.0,
        deliveryFee = 3000.0,
        discountAmount = 0.0,
        payedAmount = 40000.0,
        estimatedDeliveryMinutes = 30,
        orderDetails = OrderDetailsDto(
            customerName = "김민준",
            storeName = "맛있는 치킨",
            storeAddress = "서울시 강남구 역삼동 123-45, 1층",
            deliveryAddress = "서울시 강남구 테헤란로 123, 아파트 101동 1001호",
            items = listOf(
                OrderItemResponseDto(
                    menuName = "후라이드 치킨",
                    quantity = 1,
                    unitPrice = 18000.0,
                    totalPrice = 18000.0,
                    options = listOf(
                        OrderItemOptionResponseDto(
                            name = "소스 선택",
                            price = 0.0
                        )
                    )
                ),
                OrderItemResponseDto(
                    menuName = "양념 치킨",
                    quantity = 1,
                    unitPrice = 19000.0,
                    totalPrice = 19000.0,
                    options = emptyList()
                )
            )
        )
    )
}

/**
 * 주문 상세 정보 DTO
 */
@Schema(description = "주문 상세 정보 DTO")
data class OrderDetailsDto(
    @Schema(description = "고객명", example = "김민준", required = true)
    val customerName: String,
    
    @Schema(description = "매장명", example = "맛있는 치킨", required = true)
    val storeName: String,
    
    @Schema(description = "매장 주소", example = "서울시 강남구 역삼동 123-45, 1층", required = true)
    val storeAddress: String,
    
    @Schema(description = "배달 주소", example = "서울시 강남구 테헤란로 123, 아파트 101동 1001호", required = true)
    val deliveryAddress: String,
    
    @Schema(description = "주문 상품 목록", required = true)
    val items: List<OrderItemResponseDto>
)

/**
 * 주문 상품 응답 DTO
 */
@Schema(description = "주문 상품 응답 DTO")
data class OrderItemResponseDto(
    @Schema(description = "메뉴명", example = "후라이드 치킨", required = true)
    val menuName: String,
    
    @Schema(description = "수량", example = "1", required = true)
    val quantity: Int,
    
    @Schema(description = "단가", example = "18000", required = true)
    val unitPrice: Double,
    
    @Schema(description = "총 가격", example = "18000", required = true)
    val totalPrice: Double,
    
    @Schema(description = "옵션 목록", required = false)
    val options: List<OrderItemOptionResponseDto> = emptyList()
)

/**
 * 주문 상품 옵션 응답 DTO
 */
@Schema(description = "주문 상품 옵션 응답 DTO")
data class OrderItemOptionResponseDto(
    @Schema(description = "옵션명", example = "소스 선택", required = true)
    val name: String,
    
    @Schema(description = "옵션 가격", example = "0", required = true)
    val price: Double
)

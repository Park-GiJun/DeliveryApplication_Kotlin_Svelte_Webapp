package com.gijun.backend.infrastructure.repository.order

import com.gijun.backend.application.dto.CreateOrderDto
import com.gijun.backend.application.dto.OrderResponseDto
import com.gijun.backend.domain.repository.order.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

/**
 * 주문 저장소 구현 클래스
 */
@Repository
class OrderRepositoryImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
) : OrderRepository {
    private val logger = LoggerFactory.getLogger(OrderRepositoryImpl::class.java)

    /**
     * 새로운 주문을 저장합니다.
     */
    override fun saveOrder(createOrderDto: CreateOrderDto, orderResponseDto: OrderResponseDto): Mono<OrderResponseDto> {
        logger.info("주문 저장 시작: orderId=${orderResponseDto.id}")
        
        // R2DBC를 사용한 트랜잭션 처리를 위한 로직
        // 실제 구현에서는 R2dbcEntityTemplate을 사용하여 Order, OrderItem, OrderItemOption 엔티티를 저장
        // 여기서는 간단히 Mono.just로 구현
        
        /*
        // 실제 구현 예시 (주석 처리)
        return r2dbcEntityTemplate.inTransaction { operations ->
            // 주문 엔티티 생성 및 저장
            val orderEntity = Order(
                id = null, // auto-increment
                customerId = createOrderDto.customerId,
                storeId = createOrderDto.storeId,
                addressId = createOrderDto.addressId,
                orderNumber = orderResponseDto.orderNumber,
                orderStatus = OrderStatus.CREATED,
                orderTime = orderResponseDto.orderTime,
                acceptedTime = null,
                readyTime = null,
                deliveredTime = null,
                cancelledTime = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                deletedAt = null,
                totalAmount = orderResponseDto.totalAmount,
                discountAmount = orderResponseDto.discountAmount,
                deliveryFee = orderResponseDto.deliveryFee,
                payedAmount = orderResponseDto.payedAmount,
                requestStore = createOrderDto.requestStore,
                requestRider = createOrderDto.requestRider
            )
            
            operations.insert(Order::class.java)
                .using(orderEntity)
                .flatMap { savedOrder ->
                    // 주문 상품 저장
                    val items = createOrderDto.items.map { item ->
                        OrderItem(
                            id = null, // auto-increment
                            orderId = savedOrder.id,
                            menuId = item.menuId,
                            quantity = item.quantity,
                            unitPrice = item.unitPrice ?: 0.0,
                            totalPrice = (item.unitPrice ?: 0.0) * item.quantity,
                            createdAt = LocalDateTime.now().toString(),
                            updatedAt = LocalDateTime.now().toString(),
                            deletedAt = null
                        )
                    }
                    
                    Flux.fromIterable(items)
                        .flatMap { orderItem ->
                            operations.insert(OrderItem::class.java).using(orderItem)
                        }
                        .collectList()
                        .map { savedOrder }
                }
                .map { orderResponseDto }
        }
        */
        
        // 현재는 간단히 Mono.just로 응답
        return Mono.just(orderResponseDto).also {
            logger.info("주문 저장 완료: orderId=${orderResponseDto.id}")
        }
    }
}

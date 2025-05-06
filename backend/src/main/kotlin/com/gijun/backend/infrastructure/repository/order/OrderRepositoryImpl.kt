package com.gijun.backend.infrastructure.repository.order

import com.gijun.backend.application.dto.CreateOrderDto
import com.gijun.backend.application.dto.OrderResponseDto
import com.gijun.backend.domain.enums.order.OrderStatus
import com.gijun.backend.domain.model.order.Order
import com.gijun.backend.domain.model.order.OrderItem
import com.gijun.backend.domain.repository.order.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * 주문 저장소 구현 클래스
 */
@Repository
class OrderRepositoryImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
    private val transactionalOperator: TransactionalOperator
) : OrderRepository {
    private val logger = LoggerFactory.getLogger(OrderRepositoryImpl::class.java)

    
    override fun saveOrder(createOrderDto: CreateOrderDto, orderResponseDto: OrderResponseDto): Mono<OrderResponseDto> {
        logger.info("주문 저장 시작: orderId=${orderResponseDto.id}")
        
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
        
        return r2dbcEntityTemplate.insert(Order::class.java)
            .using(orderEntity)
            .flatMap { savedOrder ->
            // savedOrder.id가 null인지 확인
                if (savedOrder.id == null) {
                    return@flatMap Mono.error<Order>(IllegalStateException("저장된 주문의 ID가 null입니다."))
                }
                
                val items = createOrderDto.items.map { item ->
                    OrderItem(
                        id = null, // auto-increment
                        orderId = savedOrder.id, // 이제 null이 아님이 확인됨
                        menuId = item.menuId,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice ?: 0.0,
                        totalPrice = (item.unitPrice ?: 0.0) * item.quantity,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now(),
                        deletedAt = null
                    )
                }
                
                Flux.fromIterable(items)
                    .flatMap { orderItem ->
                        r2dbcEntityTemplate.insert(OrderItem::class.java).using(orderItem)
                    }
                    .collectList()
                    .map { savedOrder }
            }
            .map { orderResponseDto }
            .also {
                logger.info("주문 저장 완료: orderId=${orderResponseDto.id}")
            }
    }
}
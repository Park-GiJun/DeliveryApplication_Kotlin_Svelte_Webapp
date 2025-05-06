package com.gijun.backend.application.command.order

import com.gijun.backend.domain.enums.order.OrderStatus
import com.gijun.backend.domain.model.order.Order
import com.gijun.backend.domain.repository.order.OrderRepository
import com.gijun.backend.domain.repository.order.OrderItemRepository
import com.gijun.backend.domain.repository.order.OrderItemOptionRepository
import com.gijun.backend.infrastructure.messaging.OrderEventPublisher
import com.gijun.backend.infrastructure.cache.OrderCacheManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

/**
 * 주문 커맨드 서비스 구현 - 주문 상태를 변경하는 명령 처리
 */
@Service
class OrderCommandServiceImpl(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderItemOptionRepository: OrderItemOptionRepository,
    private val orderEventPublisher: OrderEventPublisher,
    private val orderCacheManager: OrderCacheManager
) : OrderCommandService {
    private val logger = LoggerFactory.getLogger(OrderCommandServiceImpl::class.java)

    /**
     * 새 주문 생성
     */
    override fun createOrder(command: CreateOrderCommand): Mono<Order> {
        logger.info("주문 생성 서비스 호출: 고객ID={}, 매장ID={}", command.customerId, command.storeId)
        
        // 주문 번호 생성 (현재 시간 + 랜덤 문자열)
        val orderNumber = "ORD-" + LocalDateTime.now().toString()
            .replace("[^0-9]".toRegex(), "")
            .substring(0, 12) + "-" + UUID.randomUUID().toString().substring(0, 8)
        
        // Order 객체 생성
        val order = Order(
            id = 0, // 저장 시 자동 할당
            customerId = command.customerId,
            storeId = command.storeId,
            addressId = command.addressId,
            orderNumber = orderNumber,
            orderStatus = OrderStatus.CREATED,
            orderTime = LocalDateTime.now(),
            acceptedTime = null,
            readyTime = null,
            deliveredTime = null,
            cancelledTime = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            deletedAt = null,
            totalAmount = command.totalAmount,
            discountAmount = 0.0, // 할인은 나중에 구현
            deliveryFee = command.deliveryFee,
            payedAmount = command.totalAmount + command.deliveryFee,
            requestStore = command.requestStore,
            requestRider = command.requestRider
        )
        
        // Order 저장 및 OrderItem, OrderItemOption 저장
        return orderRepository.save(order)
            .flatMap { savedOrder ->
                // OrderItem 저장
                saveOrderItems(savedOrder, command.items)
                    .then(Mono.just(savedOrder))
            }
            .doOnSuccess { savedOrder ->
                // 캐시에 주문 저장
                orderCacheManager.cacheOrder(savedOrder)
                
                // 주문 생성 이벤트 발행
                orderEventPublisher.publishOrderCreatedEvent(savedOrder)
                
                logger.info("주문 생성 완료: {}", savedOrder.orderNumber)
            }
            .doOnError { e ->
                logger.error("주문 생성 중 오류 발생: {}", e.message, e)
            }
    }

    /**
     * 주문 취소
     */
    override fun cancelOrder(orderNumber: String, reason: String): Mono<Order> {
        logger.info("주문 취소 요청: {}, 이유: {}", orderNumber, reason)
        
        return orderRepository.findByOrderNumber(orderNumber)
            .flatMap { order ->
                // 취소 가능한 상태인지 확인
                if (canCancelOrder(order)) {
                    // 주문 상태 업데이트
                    val cancelledOrder = order.copy(
                        orderStatus = OrderStatus.CANCELLED,
                        cancelledTime = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    
                    orderRepository.save(cancelledOrder)
                        .doOnSuccess { savedOrder ->
                            // 캐시 업데이트
                            orderCacheManager.cacheOrder(savedOrder)
                            
                            // 주문 취소 이벤트 발행
                            orderEventPublisher.publishOrderCancelledEvent(savedOrder, reason)
                            
                            logger.info("주문 취소 완료: {}", orderNumber)
                        }
                } else {
                    logger.warn("주문 {}는 현재 상태 {}에서 취소할 수 없습니다", orderNumber, order.orderStatus)
                    Mono.error(IllegalStateException("현재 상태(${order.orderStatus})에서는 주문을 취소할 수 없습니다"))
                }
            }
            .switchIfEmpty(Mono.error(IllegalArgumentException("주문 번호 ${orderNumber}에 해당하는 주문을 찾을 수 없습니다")))
            .doOnError { e ->
                logger.error("주문 취소 중 오류 발생: {}", e.message, e)
            }
    }

    /**
     * 주문 상태 업데이트
     */
    override fun updateOrderStatus(orderNumber: String, status: String): Mono<Order> {
        logger.info("주문 상태 업데이트 요청: {}, 상태: {}", orderNumber, status)
        
        // 유효한 주문 상태인지 확인
        val orderStatus = try {
            OrderStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            return Mono.error(IllegalArgumentException("유효하지 않은 주문 상태: $status"))
        }
        
        return orderRepository.findByOrderNumber(orderNumber)
            .flatMap { order ->
                // 상태 변경 가능 여부 확인 (실제 구현에서는 상태 전이 규칙을 더 엄격하게 적용해야 함)
                val updatedOrder = when (orderStatus) {
                    OrderStatus.ACCEPTED -> order.copy(
                        orderStatus = orderStatus,
                        acceptedTime = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    OrderStatus.READY -> order.copy(
                        orderStatus = orderStatus,
                        readyTime = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    OrderStatus.DELIVERED -> order.copy(
                        orderStatus = orderStatus,
                        deliveredTime = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    else -> order.copy(
                        orderStatus = orderStatus,
                        updatedAt = LocalDateTime.now()
                    )
                }
                
                orderRepository.save(updatedOrder)
                    .doOnSuccess { savedOrder ->
                        // 캐시 업데이트
                        orderCacheManager.cacheOrder(savedOrder)
                        
                        // 상태 변경 이벤트 발행
                        orderEventPublisher.publishOrderStatusChangedEvent(savedOrder)
                        
                        logger.info("주문 상태 업데이트 완료: {} -> {}", orderNumber, status)
                    }
            }
            .switchIfEmpty(Mono.error(IllegalArgumentException("주문 번호 ${orderNumber}에 해당하는 주문을 찾을 수 없습니다")))
            .doOnError { e ->
                logger.error("주문 상태 업데이트 중 오류 발생: {}", e.message, e)
            }
    }

    /**
     * 주문이 취소 가능한 상태인지 확인
     */
    private fun canCancelOrder(order: Order): Boolean {
        // CREATED, ACCEPTED 상태에서만 취소 가능
        return order.orderStatus == OrderStatus.CREATED || 
               order.orderStatus == OrderStatus.ACCEPTED
    }

    /**
     * 주문 항목 저장
     */
    private fun saveOrderItems(order: Order, items: List<CreateOrderCommand.OrderItemCommand>): Mono<Void> {
        logger.debug("주문 항목 저장: 주문 번호={}, 항목 수={}", order.orderNumber, items.size)
        
        val orderItems = items.map { itemCommand ->
            // OrderItem 객체 생성
            com.gijun.backend.domain.model.order.OrderItem(
                id = 0, // 저장 시 자동 할당
                orderId = order.id,
                menuId = itemCommand.menuId,
                quantity = itemCommand.quantity,
                unitPrice = itemCommand.unitPrice,
                totalPrice = itemCommand.unitPrice * itemCommand.quantity,
                createdAt = LocalDateTime.now().toString(),
                updatedAt = LocalDateTime.now().toString(),
                deletedAt = null
            )
        }
        
        // 모든 OrderItem을 저장하고, 성공하면 OrderItemOption 저장
        return reactor.core.publisher.Flux.fromIterable(orderItems)
            .flatMap { orderItem ->
                orderItemRepository.save(orderItem)
                    .flatMap { savedOrderItem ->
                        // 해당 항목의 옵션이 있는 경우, OrderItemOption 저장
                        val itemCommand = items.find { it.menuId == savedOrderItem.menuId }
                        if (itemCommand != null && itemCommand.options.isNotEmpty()) {
                            saveOrderItemOptions(order.id, savedOrderItem.id, itemCommand.options)
                        } else {
                            Mono.empty()
                        }
                    }
            }
            .then()
    }
    
    /**
     * 주문 항목 옵션 저장
     */
    private fun saveOrderItemOptions(
        orderId: Long,
        orderItemId: Long,
        options: List<CreateOrderCommand.OrderItemOptionCommand>
    ): Mono<Void> {
        logger.debug("주문 항목 옵션 저장: 주문 항목 ID={}, 옵션 수={}", orderItemId, options.size)
        
        val orderItemOptions = options.map { option ->
            // OrderItemOption 객체 생성
            com.gijun.backend.domain.model.order.OrderItemOption(
                id = 0, // 저장 시 자동 할당
                orderId = orderId,
                orderItemId = orderItemId,
                optionItemId = option.optionItemId,
                name = option.name,
                price = option.price,
                createdAt = LocalDateTime.now().toString(),
                updatedAt = LocalDateTime.now().toString(),
                deletedAt = null
            )
        }
        
        // 모든 OrderItemOption 저장
        return reactor.core.publisher.Flux.fromIterable(orderItemOptions)
            .flatMap { orderItemOption ->
                orderItemOptionRepository.save(orderItemOption)
            }
            .then()
    }
}

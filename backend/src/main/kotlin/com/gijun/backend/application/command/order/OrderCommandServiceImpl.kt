package com.gijun.backend.application.command.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.gijun.backend.application.dto.CreateOrderDto
import com.gijun.backend.application.dto.OrderResponseDto
import com.gijun.backend.domain.enums.order.OrderStatus
import com.gijun.backend.domain.repository.order.OrderRepository
import com.gijun.backend.handler.EventStreamWebSocketHandler
import com.gijun.backend.handler.StoreNotificationWebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 주문 커맨드 서비스 구현
 */
@Service
class OrderCommandServiceImpl(
    private val orderRepository: OrderRepository,
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val kafkaTemplate: KafkaTemplate<String, Any>?,
    private val objectMapper: ObjectMapper,
    private val eventStreamHandler: EventStreamWebSocketHandler,
    private val storeNotificationHandler: StoreNotificationWebSocketHandler
) : OrderCommandService {
    private val logger = LoggerFactory.getLogger(OrderCommandServiceImpl::class.java)
    private val ORDER_KAFKA_TOPIC = "delivery.orders"
    private val REDIS_ORDER_KEY_PREFIX = "order:"
    private val REDIS_STORE_ORDER_PREFIX = "store:orders:"

    /**
     * 새로운 주문을 생성합니다.
     *
     * @param createOrderDto 주문 생성 요청 DTO
     * @return 생성된 주문 정보
     */
    override fun createOrder(createOrderDto: CreateOrderDto): Mono<OrderResponseDto> {
        logger.info("주문 생성 시작: customerId=${createOrderDto.customerId}, storeId=${createOrderDto.storeId}")
        
        // 주문 번호 생성 (현재 날짜 + 시퀀스)
        val orderNumber = generateOrderNumberWithRedis()
        
        // 총 금액 계산
        val totalAmount = calculateTotalAmount(createOrderDto)
        
        // 배달비 설정 (실제로는 매장 정보에서 가져옴)
        val deliveryFee = 3000.0
        
        // 할인 금액 (실제로는 프로모션 로직에서 계산)
        val discountAmount = 0.0
        
        // 최종 결제 금액
        val payedAmount = totalAmount + deliveryFee - discountAmount
        
        // 응답 DTO 생성
        val orderResponseDto = OrderResponseDto(
            id = 0, // 저장 후 실제 ID로 대체됨
            orderNumber = orderNumber,
            status = OrderStatus.CREATED,
            orderTime = LocalDateTime.now(),
            totalAmount = totalAmount,
            deliveryFee = deliveryFee,
            discountAmount = discountAmount,
            payedAmount = payedAmount,
            estimatedDeliveryMinutes = 30 // 기본 배달 시간
        )
        
        // 주문 저장 및 응답
        return orderRepository.saveOrder(createOrderDto, orderResponseDto)
            .flatMap { savedOrder ->
                logger.info("주문 생성 성공: orderNumber=${savedOrder.orderNumber}")
                
                // 주문을 Redis에 저장
                saveOrderToRedis(savedOrder)
                    .then(addOrderToStoreQueue(createOrderDto.storeId, savedOrder.orderNumber))
                    .then(publishOrderCreatedEvent(savedOrder))
                    .then(sendOrderNotificationToStore(createOrderDto.storeId, savedOrder))
                    .thenReturn(savedOrder)
            }
            .doOnError { error ->
                logger.error("주문 생성 실패: ${error.message}", error)
            }
    }

    /**
     * Redis를 사용하여 주문 번호를 생성합니다.
     * 형식: ORD-YYYYMMDD-SEQ
     *
     * @return 생성된 주문 번호
     */
    private fun generateOrderNumberWithRedis(): String {
        val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val orderSeqKey = "seq:order:$dateStr"
        
        return redisTemplate.opsForValue().increment(orderSeqKey)
            .defaultIfEmpty(1L)
            .map { seq ->
                "ORD-$dateStr-${seq.toString().padStart(3, '0')}"
            }
            .doOnError { error ->
                logger.error("Redis 주문 시퀀스 생성 실패, 대체 방법 사용: ${error.message}")
            }
            .onErrorResume {
                // Redis 실패 시 랜덤 방식으로 폴백
                val randomSeq = (Math.random() * 1000).toInt().toString().padStart(3, '0')
                Mono.just("ORD-$dateStr-$randomSeq")
            }
            .block() ?: "ORD-$dateStr-${(Math.random() * 1000).toInt().toString().padStart(3, '0')}"
    }

    /**
     * 주문 정보를 Redis에 저장합니다.
     *
     * @param order 주문 정보
     * @return Mono<Boolean> 저장 결과
     */
    private fun saveOrderToRedis(order: OrderResponseDto): Mono<Boolean> {
        val orderKey = "$REDIS_ORDER_KEY_PREFIX${order.orderNumber}"
        val orderJson = objectMapper.writeValueAsString(order)
        
        logger.debug("Redis에 주문 저장: $orderKey")
        return redisTemplate.opsForValue().set(orderKey, orderJson)
            .doOnSuccess { success ->
                if (success) {
                    logger.debug("Redis에 주문 저장 성공: $orderKey")
                } else {
                    logger.warn("Redis에 주문 저장 실패: $orderKey")
                }
            }
            .doOnError { error ->
                logger.error("Redis에 주문 저장 중 오류: ${error.message}", error)
            }
            .onErrorResume { Mono.just(false) }
    }

    /**
     * 생성된 주문을 매장의 주문 큐에 추가합니다.
     *
     * @param storeId 매장 ID
     * @param orderNumber 주문 번호
     * @return Mono<Long> 큐에 추가된 결과
     */
    private fun addOrderToStoreQueue(storeId: Long, orderNumber: String): Mono<Long> {
        val storeOrdersKey = "$REDIS_STORE_ORDER_PREFIX$storeId"
        
        logger.debug("매장 주문 큐에 추가: $storeOrdersKey, $orderNumber")
        return redisTemplate.opsForList().rightPush(storeOrdersKey, orderNumber)
            .doOnSuccess { result ->
                logger.debug("매장 주문 큐에 추가 성공: $storeOrdersKey, $orderNumber, 큐 크기: $result")
            }
            .doOnError { error ->
                logger.error("매장 주문 큐에 추가 중 오류: ${error.message}", error)
            }
            .onErrorResume { Mono.just(0L) }
    }

    /**
     * 주문 생성 이벤트를 Kafka에 발행합니다.
     *
     * @param order 주문 정보
     * @return Mono<Void> 완료 시그널
     */
    private fun publishOrderCreatedEvent(order: OrderResponseDto): Mono<Void> {
        if (kafkaTemplate == null) {
            logger.warn("Kafka가 구성되지 않았습니다. 주문 이벤트를 발행할 수 없습니다.")
            return Mono.empty()
        }
        
        val orderEvent = mapOf(
            "eventType" to "ORDER_CREATED",
            "orderNumber" to order.orderNumber,
            "timestamp" to LocalDateTime.now().toString(),
            "data" to order
        )
        
        logger.debug("Kafka에 주문 생성 이벤트 발행: ${order.orderNumber}")
        try {
            kafkaTemplate.send(ORDER_KAFKA_TOPIC, order.orderNumber, orderEvent)
            logger.info("Kafka에 주문 생성 이벤트 발행 성공: ${order.orderNumber}")
        } catch (e: Exception) {
            logger.error("Kafka에 주문 생성 이벤트 발행 실패: ${e.message}", e)
        }
        
        return Mono.empty()
    }

    /**
     * WebSocket을 통해 매장에 주문 알림을 전송합니다.
     *
     * @param storeId 매장 ID
     * @param order 주문 정보
     * @return Mono<Void> 완료 시그널
     */
    private fun sendOrderNotificationToStore(storeId: Long, order: OrderResponseDto): Mono<Void> {
        val notification = mapOf(
            "eventType" to "NEW_ORDER_NOTIFICATION",
            "storeId" to storeId,
            "orderNumber" to order.orderNumber,
            "timestamp" to LocalDateTime.now().toString(),
            "data" to order
        )
        
        logger.debug("WebSocket을 통해 매장에 주문 알림 전송: storeId=$storeId, orderNumber=${order.orderNumber}")
        
        // 전체 이벤트 스트림으로 알림 전송 (대시보드용)
        eventStreamHandler.sendEvent(notification)
        
        // 해당 매장에만 주문 알림 전송 (매장 앱용)
        storeNotificationHandler.sendEventToStore(storeId, notification)
        
        return Mono.empty()
    }

    /**
     * 주문 총 금액을 계산합니다.
     *
     * @param createOrderDto 주문 생성 요청 DTO
     * @return 계산된 총 금액
     */
    private fun calculateTotalAmount(createOrderDto: CreateOrderDto): Double {
        return createOrderDto.items.sumOf { item -> 
            val optionsPrice = item.options?.sumOf { option -> option.price } ?: 0.0
            (item.unitPrice + optionsPrice) * item.quantity
        }
    }

    /**
     * 주문을 취소합니다.
     * 
     * @param orderNumber 주문 번호
     * @param reason 취소 사유
     * @return 취소된 주문 정보
     */
    override fun cancelOrder(orderNumber: String, reason: String): Mono<OrderResponseDto> {
        logger.info("주문 취소 요청: orderNumber=$orderNumber, reason=$reason")
        
        // Redis에서 주문 정보 조회
        return getOrderFromRedis(orderNumber)
            .flatMap { order ->
                // 주문 상태를 취소로 변경
                val cancelledOrder = order.copy(
                    status = OrderStatus.CANCELLED
                )
                
                // Redis에 업데이트
                saveOrderToRedis(cancelledOrder)
                    .flatMap { 
                        // Kafka에 취소 이벤트 발행
                        publishOrderCancelledEvent(cancelledOrder, reason)
                            .thenReturn(cancelledOrder)
                    }
            }
            .switchIfEmpty(Mono.error(RuntimeException("주문을 찾을 수 없습니다: $orderNumber")))
    }

    /**
     * Redis에서 주문 정보를 조회합니다.
     *
     * @param orderNumber 주문 번호
     * @return Mono<OrderResponseDto> 주문 정보
     */
    private fun getOrderFromRedis(orderNumber: String): Mono<OrderResponseDto> {
        val orderKey = "$REDIS_ORDER_KEY_PREFIX$orderNumber"
        
        return redisTemplate.opsForValue().get(orderKey)
            .flatMap { orderJson ->
                try {
                    val order = objectMapper.readValue(orderJson, OrderResponseDto::class.java)
                    Mono.just(order)
                } catch (e: Exception) {
                    logger.error("주문 JSON 파싱 오류: ${e.message}", e)
                    Mono.empty()
                }
            }
    }

    /**
     * 주문 취소 이벤트를 Kafka에 발행합니다.
     *
     * @param order 취소된 주문 정보
     * @param reason 취소 사유
     * @return Mono<Void> 완료 시그널
     */
    private fun publishOrderCancelledEvent(order: OrderResponseDto, reason: String): Mono<Void> {
        if (kafkaTemplate == null) {
            logger.warn("Kafka가 구성되지 않았습니다. 주문 취소 이벤트를 발행할 수 없습니다.")
            return Mono.empty()
        }
        
        val orderEvent = mapOf(
            "eventType" to "ORDER_CANCELLED",
            "orderNumber" to order.orderNumber,
            "timestamp" to LocalDateTime.now().toString(),
            "reason" to reason,
            "data" to order
        )
        
        logger.debug("Kafka에 주문 취소 이벤트 발행: ${order.orderNumber}")
        try {
            // Kafka 이벤트 발행
            kafkaTemplate.send(ORDER_KAFKA_TOPIC, order.orderNumber, orderEvent)
            logger.info("Kafka에 주문 취소 이벤트 발행 성공: ${order.orderNumber}")
            
            // 대시보드용 WebSocket 알림 전송
            eventStreamHandler.sendEvent(orderEvent)
            
            // 해당 매장에도 취소 알림 전송
            order.orderDetails?.let { details ->
                // Redis에서 주문 상세 정보를 조회하여 storeId 확인
                // 여기서는 임시로 주문 번호에서 storeId를 추출한다고 가정
                val storeId = getStoreIdFromRedis(order.orderNumber)
                storeId?.let { 
                    storeNotificationHandler.sendEventToStore(it, orderEvent)
                }
            }
        } catch (e: Exception) {
            logger.error("Kafka에 주문 취소 이벤트 발행 실패: ${e.message}", e)
        }
        
        return Mono.empty()
    }
    
    /**
     * Redis에서 주문 정보를 조회하여 매장 ID를 반환합니다.
     * 실제 구현 시에는 Redis에 저장된 주문 정보에서 storeId를 추출해야 합니다.
     */
    private fun getStoreIdFromRedis(orderNumber: String): Long? {
        // 임시 구현: 주문 번호 해싱하여 매장 ID 생성 (1~10 범위)
        return (orderNumber.hashCode() % 10 + 1).toLong()
    }

    /**
     * 주문 상태를 업데이트합니다.
     * 
     * @param orderNumber 주문 번호
     * @param status 변경할 상태
     * @return 업데이트된 주문 정보
     */
    override fun updateOrderStatus(orderNumber: String, status: String): Mono<OrderResponseDto> {
        logger.info("주문 상태 변경 요청: orderNumber=$orderNumber, status=$status")
        
        // 상태 유효성 검사
        val orderStatus = try {
            OrderStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            logger.error("잘못된 주문 상태: $status")
            return Mono.error(IllegalArgumentException("잘못된 주문 상태: $status"))
        }
        
        // Redis에서 주문 정보 조회
        return getOrderFromRedis(orderNumber)
            .flatMap { order ->
                // 주문 상태를 업데이트
                val updatedOrder = order.copy(
                    status = orderStatus
                )
                
                // Redis에 업데이트
                saveOrderToRedis(updatedOrder)
                    .flatMap { 
                        // Kafka에 상태 변경 이벤트 발행
                        publishOrderStatusChangedEvent(updatedOrder)
                            .thenReturn(updatedOrder)
                    }
            }
            .switchIfEmpty(Mono.error(RuntimeException("주문을 찾을 수 없습니다: $orderNumber")))
    }

    /**
     * 주문 상태 변경 이벤트를 Kafka에 발행합니다.
     *
     * @param order 업데이트된 주문 정보
     * @return Mono<Void> 완료 시그널
     */
    private fun publishOrderStatusChangedEvent(order: OrderResponseDto): Mono<Void> {
        if (kafkaTemplate == null) {
            logger.warn("Kafka가 구성되지 않았습니다. 주문 상태 변경 이벤트를 발행할 수 없습니다.")
            return Mono.empty()
        }
        
        val eventType = when (order.status) {
            OrderStatus.ASSIGNED -> "ORDER_ASSIGNED"
            OrderStatus.ACCEPTED -> "ORDER_ACCEPTED"
            OrderStatus.REJECTED -> "ORDER_REJECTED"
            OrderStatus.READY -> "ORDER_READY"
            OrderStatus.PICKED_UP -> "ORDER_PICKED_UP"
            OrderStatus.DELIVERED -> "ORDER_DELIVERED"
            else -> "ORDER_STATUS_CHANGED"
        }
        
        val orderEvent = mapOf(
            "eventType" to eventType,
            "orderNumber" to order.orderNumber,
            "timestamp" to LocalDateTime.now().toString(),
            "status" to order.status.name,
            "data" to order
        )
        
        logger.debug("Kafka에 주문 상태 변경 이벤트 발행: ${order.orderNumber}, 상태: ${order.status}")
        try {
            kafkaTemplate.send(ORDER_KAFKA_TOPIC, order.orderNumber, orderEvent)
            logger.info("Kafka에 주문 상태 변경 이벤트 발행 성공: ${order.orderNumber}, 상태: ${order.status}")

            // WebSocket 알림도 전송
            eventStreamHandler.sendEvent(orderEvent)
        } catch (e: Exception) {
            logger.error("Kafka에 주문 상태 변경 이벤트 발행 실패: ${e.message}", e)
        }
        
        return Mono.empty()
    }
}
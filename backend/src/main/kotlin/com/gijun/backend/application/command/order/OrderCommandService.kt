package com.gijun.backend.application.command.order

import com.gijun.backend.application.dto.CreateOrderDto
import com.gijun.backend.application.dto.OrderResponseDto
import com.gijun.backend.domain.enums.order.OrderStatus
import com.gijun.backend.domain.repository.order.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * 주문 Command 서비스 구현 클래스
 */
@Service
class OrderCommandService(
    private val orderRepository: OrderRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>?
) {
    private val logger = LoggerFactory.getLogger(OrderCommandService::class.java)
    
    /**
     * 새 주문을 생성합니다.
     */
    fun createOrder(createOrderDto: CreateOrderDto): Mono<OrderResponseDto> {
        logger.info("주문 생성 서비스 호출: customerId=${createOrderDto.customerId}, storeId=${createOrderDto.storeId}")
        
        // 주문 ID 생성
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val orderDateStr = LocalDateTime.now().format(dateFormatter)
        val orderId = "ORD$orderDateStr${(1000..9999).random()}"
        
        // 주문 번호 생성
        val orderNumber = "A$orderDateStr-${(100..999).random()}"
        
        // 총 상품 금액 계산
        val totalAmount = createOrderDto.items.sumOf { item -> 
            (item.unitPrice ?: 0.0) * item.quantity
        }
        
        // 배달료 계산
        val deliveryFee = 3000.0
        
        // 할인 금액
        val discountAmount = 0.0
        
        // 최종 결제 금액
        val payedAmount = totalAmount + deliveryFee - discountAmount
        
        // 예상 배달 시간 (분)
        val estimatedDeliveryMinutes = Random.nextInt(25, 45)
        
        // OrderResponseDto 생성
        val orderResponse = OrderResponseDto(
            id = orderId.toLong(),
            orderNumber = orderNumber,
            status = OrderStatus.CREATED,
            orderTime = LocalDateTime.now(),
            totalAmount = totalAmount,
            deliveryFee = deliveryFee,
            discountAmount = discountAmount,
            payedAmount = payedAmount,
            estimatedDeliveryMinutes = estimatedDeliveryMinutes,
            orderDetails = null
        )
        
        // 저장소에 주문 저장
        return orderRepository.saveOrder(createOrderDto, orderResponse)
            .flatMap { savedOrder ->
                // Kafka가 구성되어 있다면 이벤트 발행
                if (kafkaTemplate != null) {
                    try {
                        logger.info("Kafka 이벤트 발행: order.created, orderId=$orderId")
                        val event = mapOf(
                            "type" to "order.created",
                            "orderId" to orderId,
                            "data" to orderResponse,
                            "timestamp" to LocalDateTime.now().toString()
                        )
                        kafkaTemplate.send("delivery.orders", orderId, event)
                    } catch (e: Exception) {
                        logger.error("Kafka 이벤트 발행 실패: ${e.message}", e)
                        // 발행 실패해도 주문 처리는 계속함
                    }
                }
                
                logger.info("주문 생성 완료: orderId=$orderId")
                Mono.just(orderResponse)
            }
    }
}

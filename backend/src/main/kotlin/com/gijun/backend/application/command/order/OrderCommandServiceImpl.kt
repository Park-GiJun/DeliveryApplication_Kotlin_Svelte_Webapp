package com.gijun.backend.application.command.order

import com.gijun.backend.application.dto.CreateOrderDto
import com.gijun.backend.application.dto.OrderResponseDto
import com.gijun.backend.domain.repository.order.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.gijun.backend.domain.enums.order.OrderStatus

/**
 * 주문 커맨드 서비스 구현
 */
@Service
class OrderCommandServiceImpl(
    private val orderRepository: OrderRepository
) : OrderCommandService {
    private val logger = LoggerFactory.getLogger(OrderCommandServiceImpl::class.java)

    /**
     * 새로운 주문을 생성합니다.
     *
     * @param createOrderDto 주문 생성 요청 DTO
     * @return 생성된 주문 정보
     */
    override fun createOrder(createOrderDto: CreateOrderDto): Mono<OrderResponseDto> {
        logger.info("주문 생성 시작: customerId=${createOrderDto.customerId}, storeId=${createOrderDto.storeId}")
        
        // 주문 번호 생성 (현재 날짜 + 시퀀스)
        val orderNumber = generateOrderNumber()
        
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
            .doOnSuccess { savedOrder ->
                logger.info("주문 생성 성공: orderNumber=${savedOrder.orderNumber}")
            }
            .doOnError { error ->
                logger.error("주문 생성 실패: ${error.message}", error)
            }
    }

    /**
     * 주문 번호를 생성합니다.
     * 형식: ORD-YYYYMMDD-SEQ
     *
     * @return 생성된 주문 번호
     */
    private fun generateOrderNumber(): String {
        val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        // 실제 구현에서는 시퀀스 생성 로직 필요 (Redis incr 등을 활용)
        val sequence = (Math.random() * 1000).toInt().toString().padStart(3, '0')
        return "ORD-$dateStr-$sequence"
    }

    /**
     * 주문 총 금액을 계산합니다.
     *
     * @param createOrderDto 주문 생성 요청 DTO
     * @return 계산된 총 금액
     */
    private fun calculateTotalAmount(createOrderDto: CreateOrderDto): Double {
        return createOrderDto.items.sumOf { item -> 
            item.unitPrice * item.quantity
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
        
        // 실제 구현 필요
        return Mono.empty()
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
        
        // 실제 구현 필요
        return Mono.empty()
    }
}
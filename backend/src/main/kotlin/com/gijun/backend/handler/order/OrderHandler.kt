package com.gijun.backend.handler.order

import com.gijun.backend.application.command.order.CreateOrderCommand
import com.gijun.backend.application.command.order.OrderCommandService
import com.gijun.backend.application.query.order.OrderQueryService
import com.gijun.backend.domain.dto.order.OrderCancelRequestDTO
import com.gijun.backend.domain.dto.order.OrderDetailResponseDTO
import com.gijun.backend.domain.dto.order.OrderListResponseDTO
import com.gijun.backend.domain.dto.order.OrderRequestDTO
import com.gijun.backend.domain.dto.order.OrderResponseDTO
import com.gijun.backend.domain.dto.order.OrderSummaryResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

/**
 * 주문 핸들러 - 웹 요청을 받아 적절한 애플리케이션 서비스로 전달
 */
@Component
class OrderHandler(
    private val orderQueryService: OrderQueryService,
    private val orderCommandService: OrderCommandService
) {
    private val logger = LoggerFactory.getLogger(OrderHandler::class.java)

    /**
     * 주문 생성 요청 처리
     */
    fun createOrder(request: ServerRequest): Mono<ServerResponse> {
        logger.info("주문 생성 요청 처리")
        
        return request.bodyToMono(OrderRequestDTO::class.java)
            .switchIfEmpty(Mono.error(IllegalArgumentException("요청 본문이 비어있습니다")))
            .flatMap { orderRequestDTO ->
                // DTO를 Command로 변환
                val command = CreateOrderCommand(
                    customerId = orderRequestDTO.customerId,
                    storeId = orderRequestDTO.storeId,
                    addressId = orderRequestDTO.addressId,
                    items = orderRequestDTO.items.map { item ->
                        CreateOrderCommand.OrderItemCommand(
                            menuId = item.menuId,
                            quantity = item.quantity,
                            unitPrice = item.unitPrice,
                            options = item.options?.map { option ->
                                CreateOrderCommand.OrderItemOptionCommand(
                                    optionItemId = option.optionItemId,
                                    name = option.name,
                                    price = option.price
                                )
                            } ?: emptyList()
                        )
                    },
                    totalAmount = orderRequestDTO.totalAmount,
                    deliveryFee = orderRequestDTO.deliveryFee,
                    requestStore = orderRequestDTO.requestStore,
                    requestRider = orderRequestDTO.requestRider
                )
                
                // 커맨드 서비스 호출
                orderCommandService.createOrder(command)
                    .flatMap { order ->
                        ServerResponse.ok()
                            .bodyValue(
                                OrderResponseDTO(
                                    success = true,
                                    orderId = order.id.toInt(),
                                    orderNumber = order.orderNumber,
                                    message = "주문이 성공적으로 생성되었습니다."
                                )
                            )
                    }
            }
            .onErrorResume { e ->
                logger.error("주문 생성 중 오류 발생: {}", e.message, e)
                ServerResponse.badRequest()
                    .bodyValue(
                        OrderResponseDTO(
                            success = false,
                            orderId = null,
                            orderNumber = null,
                            message = "주문 생성 실패: ${e.message}"
                        )
                    )
            }
    }

    /**
     * 주문 번호로 주문 정보 조회
     */
    fun getOrderByNumber(request: ServerRequest): Mono<ServerResponse> {
        val orderNumber = request.pathVariable("orderNumber")
        logger.info("주문 조회 요청: {}", orderNumber)
        
        return orderQueryService.getOrderByNumber(orderNumber)
            .flatMap { orderDetail ->
                ServerResponse.ok()
                    .bodyValue(OrderDetailResponseDTO.fromOrderDetail(orderDetail))
            }
            .switchIfEmpty(
                ServerResponse.notFound().build()
            )
            .onErrorResume { e ->
                logger.error("주문 조회 중 오류 발생: {}", e.message, e)
                ServerResponse.badRequest()
                    .bodyValue(
                        OrderDetailResponseDTO(
                            success = false,
                            message = "주문 조회 실패: ${e.message}"
                        )
                    )
            }
    }

    /**
     * 고객 ID로 주문 목록 조회
     */
    fun getOrdersByCustomer(request: ServerRequest): Mono<ServerResponse> {
        val customerId = request.pathVariable("customerId").toInt()
        val page = request.queryParam("page").map { it.toInt() }.orElse(0)
        val size = request.queryParam("size").map { it.toInt() }.orElse(10)
        
        logger.info("고객 {} 주문 목록 조회 요청 - page: {}, size: {}", customerId, page, size)
        
        return orderQueryService.getOrdersByCustomerId(customerId, page, size)
            .collectList()
            .flatMap { orders ->
                ServerResponse.ok()
                    .bodyValue(
                        OrderListResponseDTO(
                            success = true,
                            orders = orders.map { OrderSummaryResponseDTO.fromOrderSummary(it) },
                            message = "${orders.size}개의 주문이 조회되었습니다."
                        )
                    )
            }
            .switchIfEmpty(
                ServerResponse.ok()
                    .bodyValue(
                        OrderListResponseDTO(
                            success = true,
                            orders = emptyList(),
                            message = "주문 내역이 없습니다."
                        )
                    )
            )
            .onErrorResume { e ->
                logger.error("주문 목록 조회 중 오류 발생: {}", e.message, e)
                ServerResponse.badRequest()
                    .bodyValue(
                        OrderListResponseDTO(
                            success = false,
                            orders = emptyList(),
                            message = "주문 목록 조회 실패: ${e.message}"
                        )
                    )
            }
    }

    /**
     * 주문 취소 처리
     */
    fun cancelOrder(request: ServerRequest): Mono<ServerResponse> {
        val orderNumber = request.pathVariable("orderNumber")
        
        logger.info("주문 취소 요청: {}", orderNumber)
        
        return request.bodyToMono(OrderCancelRequestDTO::class.java)
            .switchIfEmpty(Mono.just(OrderCancelRequestDTO("")))
            .flatMap { cancelRequest ->
                orderCommandService.cancelOrder(orderNumber, cancelRequest.reason)
                    .flatMap { order ->
                        ServerResponse.ok()
                            .bodyValue(
                                OrderResponseDTO(
                                    success = true,
                                    orderId = order.id.toInt(),
                                    orderNumber = order.orderNumber,
                                    message = "주문이 취소되었습니다."
                                )
                            )
                    }
            }
            .switchIfEmpty(
                ServerResponse.notFound().build()
            )
            .onErrorResume { e ->
                logger.error("주문 취소 중 오류 발생: {}", e.message, e)
                ServerResponse.badRequest()
                    .bodyValue(
                        OrderResponseDTO(
                            success = false,
                            orderId = null,
                            orderNumber = orderNumber,
                            message = "주문 취소 실패: ${e.message}"
                        )
                    )
            }
    }
}

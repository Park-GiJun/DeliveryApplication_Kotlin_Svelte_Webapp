package com.gijun.backend.handler.order

import com.gijun.backend.application.command.order.OrderCommandService
import com.gijun.backend.application.dto.CreateOrderDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI

/**
 * 주문 처리를 위한 핸들러
 */
@Component
class OrderHandler(private val orderCommandService: OrderCommandService) {
    private val logger = LoggerFactory.getLogger(OrderHandler::class.java)

    /**
     * 새로운 주문을 생성합니다.
     */
    @Operation(
        summary = "주문 생성",
        description = "새로운 주문을 생성합니다",
        tags = ["orders"],
        requestBody = RequestBody(
            required = true,
            content = [Content(schema = Schema(implementation = CreateOrderDto::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "주문 생성 성공",
                content = [Content(schema = Schema(implementation = com.gijun.backend.application.dto.OrderResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류"
            )
        ]
    )
    fun createOrder(request: ServerRequest): Mono<ServerResponse> {
        logger.info("주문 생성 요청 수신")
        
        return request.bodyToMono<CreateOrderDto>()
            .flatMap { createOrderDto ->
                orderCommandService.createOrder(createOrderDto)
                    .flatMap { order ->
                        logger.info("주문 생성 성공: orderNumber=${order.orderNumber}")
                        ServerResponse.created(URI.create("/api/orders/${order.orderNumber}"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(order)
                    }
            }
            .onErrorResume { e ->
                logger.error("주문 생성 실패: ${e.message}", e)
                ServerResponse.badRequest().bodyValue(mapOf(
                    "status" to "error",
                    "message" to (e.message ?: "주문 생성 중 오류가 발생했습니다")
                ))
            }
    }

    /**
     * 주문 번호로 주문을 조회합니다.
     */
    @Operation(
        summary = "주문 조회",
        description = "주문 번호로 주문을 조회합니다",
        tags = ["orders"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "주문 조회 성공",
                content = [Content(schema = Schema(implementation = com.gijun.backend.application.dto.OrderResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "주문을 찾을 수 없음"
            )
        ]
    )
    fun getOrderByNumber(request: ServerRequest): Mono<ServerResponse> {
        val orderNumber = request.pathVariable("orderNumber")
        logger.info("주문 조회 요청: orderNumber=$orderNumber")
        
        // 주문 조회 서비스는 아직 미구현
        return ServerResponse.ok().bodyValue(mapOf(
            "message" to "아직 구현되지 않은 기능입니다: 주문 조회",
            "orderNumber" to orderNumber
        ))
    }

    /**
     * 고객 ID로 주문 목록을 조회합니다.
     */
    @Operation(
        summary = "고객별 주문 목록 조회",
        description = "고객 ID로 주문 목록을 조회합니다",
        tags = ["orders"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "주문 목록 조회 성공"
            ),
            ApiResponse(
                responseCode = "404",
                description = "고객을 찾을 수 없음"
            )
        ]
    )
    fun getOrdersByCustomer(request: ServerRequest): Mono<ServerResponse> {
        val customerId = request.pathVariable("customerId")
        logger.info("고객별 주문 목록 조회 요청: customerId=$customerId")
        
        // 고객별 주문 목록 조회 서비스는 아직 미구현
        return ServerResponse.ok().bodyValue(mapOf(
            "message" to "아직 구현되지 않은 기능입니다: 고객별 주문 목록 조회",
            "customerId" to customerId
        ))
    }

    /**
     * 주문을 취소합니다.
     */
    @Operation(
        summary = "주문 취소",
        description = "주문 번호로 주문을 취소합니다",
        tags = ["orders"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "주문 취소 성공"
            ),
            ApiResponse(
                responseCode = "404",
                description = "주문을 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "400",
                description = "취소할 수 없는 상태"
            )
        ]
    )
    fun cancelOrder(request: ServerRequest): Mono<ServerResponse> {
        val orderNumber = request.pathVariable("orderNumber")
        logger.info("주문 취소 요청: orderNumber=$orderNumber")
        
        // 주문 취소 서비스는 아직 미구현
        return request.bodyToMono<Map<String, String>>()
            .flatMap { body -> 
                val reason = body["reason"] ?: "고객 요청에 의한 취소"
                orderCommandService.cancelOrder(orderNumber, reason)
                    .flatMap { order ->
                        ServerResponse.ok().bodyValue(order)
                    }
            }
            .switchIfEmpty(
                orderCommandService.cancelOrder(orderNumber, "고객 요청에 의한 취소")
                    .flatMap { order ->
                        ServerResponse.ok().bodyValue(order)
                    }
            )
            .onErrorResume { e ->
                ServerResponse.badRequest().bodyValue(mapOf(
                    "status" to "error",
                    "message" to (e.message ?: "주문 취소 중 오류가 발생했습니다")
                ))
            }
    }
}
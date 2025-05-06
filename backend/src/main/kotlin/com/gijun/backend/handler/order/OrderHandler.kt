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
                        logger.info("주문 생성 성공: id=${order.id}")
                        ServerResponse.created(URI.create("/api/orders/${order.id}"))
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
}

package com.gijun.backend.adapter.order

import com.gijun.backend.application.dto.CreateOrderDto
import com.gijun.backend.application.dto.OrderItemDto
import com.gijun.backend.application.dto.OrderItemOptionDto
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * 테스트용 주문 API 라우터 - 간단한 주문 생성 테스트 기능 제공
 */
@Configuration
class OrderTestAdapter {
    private val logger = LoggerFactory.getLogger(OrderTestAdapter::class.java)

    @Bean
    fun orderTestRoutes(): RouterFunction<ServerResponse> {
        logger.info("주문 테스트 어댑터 라우터 초기화")
        
        return router {
            "/api/test/orders".nest {
                GET("/sample") { request ->
                    // 샘플 주문 DTO 생성
                    val sampleOrder = CreateOrderDto(
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
                    
                    // 응답 생성
                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sampleOrder)
                }
            }
        }.also {
            logger.info("주문 테스트 API 라우팅 설정 완료: /api/test/orders")
        }
    }
}
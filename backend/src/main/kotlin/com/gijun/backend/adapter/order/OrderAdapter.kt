package com.gijun.backend.adapter.order

import com.gijun.backend.handler.order.OrderHandler
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

/**
 * 주문 API 라우터 - 외부 요청을 적절한 핸들러로 연결하는 어댑터
 */
@Configuration
class OrderAdapter(private val orderHandler: OrderHandler) {
    private val logger = LoggerFactory.getLogger(OrderAdapter::class.java)

    @Bean
    fun orderRoutes(): RouterFunction<ServerResponse> {
        logger.info("주문 어댑터 라우터 초기화")
        
        return router {
            "/api/orders".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    POST("", orderHandler::createOrder)
                    GET("/{orderNumber}", orderHandler::getOrderByNumber)
                    GET("/customer/{customerId}", orderHandler::getOrdersByCustomer)
                    POST("/{orderNumber}/cancel", orderHandler::cancelOrder)
                    // 매장 주문 관련 API 추가
                    GET("/store/{storeId}", orderHandler::getOrdersByStore)
                    POST("/{orderNumber}/status", orderHandler::updateOrderStatus)
                }
            }
        }.also {
            logger.info("주문 API 라우팅 설정 완료: /api/orders")
        }
    }
}

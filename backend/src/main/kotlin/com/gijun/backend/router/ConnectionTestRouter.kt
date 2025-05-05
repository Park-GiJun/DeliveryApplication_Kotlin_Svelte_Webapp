package com.gijun.backend.router

import com.gijun.backend.adapter.ConnectionTestHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

/**
 * 연결 테스트를 위한 라우팅 설정
 */
@Configuration
class ConnectionTestRouter(private val connectionTestHandler: ConnectionTestHandler) {

    @Bean
    fun connectionTestRoutes(): RouterFunction<ServerResponse> {
        return router {
            "/api/connection-test".nest {
                GET("/status", connectionTestHandler::getConnectionStatus)
                GET("/database", connectionTestHandler::testDatabaseConnection)
                POST("/redis", connectionTestHandler::testRedisConnection)
                POST("/kafka", connectionTestHandler::testKafkaConnection)
                GET("/stream", accept(MediaType.APPLICATION_JSON), connectionTestHandler::testReactiveStream)
            }
        }
    }
}

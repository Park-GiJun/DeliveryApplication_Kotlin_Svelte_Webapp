package com.gijun.backend.adapter

import com.gijun.backend.handler.ConnectionTestHandler
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(ConnectionTestRouter::class.java)

    @Bean
    fun connectionTestRoutes(): RouterFunction<ServerResponse> {
        logger.info("ConnectionTest 라우터 초기화")
        
        return router {
            "/api/connection-test".nest {
                GET("/status", connectionTestHandler::getConnectionStatus)
                GET("/database", connectionTestHandler::testDatabaseConnection)
                POST("/redis", connectionTestHandler::testRedisConnection)
                POST("/kafka", connectionTestHandler::testKafkaConnection)
                GET("/stream", accept(MediaType.APPLICATION_JSON), connectionTestHandler::testReactiveStream)
            }
        }.also {
            logger.info("ConnectionTest 라우터 설정 완료: /api/connection-test/*")
        }
    }
}

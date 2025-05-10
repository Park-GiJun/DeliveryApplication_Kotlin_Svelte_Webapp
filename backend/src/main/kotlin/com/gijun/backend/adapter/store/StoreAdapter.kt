package com.gijun.backend.adapter.store

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class StoreAdapter(private val storeHandler : StoreHandler) {
    private val logger = org.slf4j.LoggerFactory.getLogger(StoreAdapter::class.java)

    @Bean
    fun stroeRoutes(): RouterFunction<ServerResponse> {
        logger.info("매장 어댑터 라우터 초기화")

        return router {
            "api/stores".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("", storeHandler::getAllStores)
                    GET("/{storeId}", storeHandler::getStoreById)
                    GET("/{storeId}/menu",storeHandler::getStoreMenu)
                }
            }
        }.also {
            logger.info("매장 API 라우팅 설정 완료: /api/stores")
        }
    }
}
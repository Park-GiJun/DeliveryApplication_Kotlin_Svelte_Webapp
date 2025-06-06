package com.gijun.backend.adapter.store

import com.gijun.backend.handler.store.StoreHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class StoreAdapter(private val storeHandler: StoreHandler) {
    private val logger = org.slf4j.LoggerFactory.getLogger(StoreAdapter::class.java)

    @Bean
    fun storeRoutes(): RouterFunction<ServerResponse> {  // 오타 수정: stroeRoutes -> storeRoutes
        logger.info("매장 어댑터 라우터 초기화")

        return router {
            "/api/stores".nest {  // 경로 수정: api/stores -> /api/stores
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("", storeHandler::getAllStores)
                    GET("/{storeId}", storeHandler::getStoreById)
                    GET("/{storeId}/menu", storeHandler::getStoreMenu)
                }
            }
        }.also {
            logger.info("매장 API 라우팅 설정 완료: /api/stores")
        }
    }
}
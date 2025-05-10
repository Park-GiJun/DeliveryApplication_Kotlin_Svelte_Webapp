package com.gijun.backend.handler.store

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class StoreHandler(private val storeQueryService: StoreQueryService) {
    private val logger = org.slf4j.LoggerFactory.getLogger(StoreHandler::class.java)

    fun getAllStores(request: ServerRequest): Mono<ServerResponse> {
        logger.info("모든 매장 조회 요청")
        return storeQueryService.getAllStores()
            .flatMap{stores->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(stores)
            }
    }

    fun getStoreById(request:ServerRequest): Mono<ServerResponse> {
        val storeId = request.pathVariable("storeId").toLong()
        logger.info("매장 조회 요청: storeId=${storeId}")

        return storeQueryService.getStoreById(storeId)
            .flatMap { store ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(store)
            }

    }
}
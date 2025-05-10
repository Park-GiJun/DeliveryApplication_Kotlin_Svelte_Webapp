package com.gijun.backend.domain.repository.store

import com.gijun.backend.application.dto.MenuItemDTO
import com.gijun.backend.application.dto.StoreDTO
import reactor.core.publisher.Mono

interface StoreRepository {
    fun findAllStores(): Mono<List<StoreDTO>>
    fun findStoreById(storeId: Long): Mono<StoreDTO>
    fun findMenuByStoreId(storeId: Long): Mono<List<MenuItemDTO>>
}
package com.gijun.backend.application.query.store

import com.gijun.backend.application.dto.MenuItemDTO
import com.gijun.backend.application.dto.StoreDTO
import reactor.core.publisher.Mono

interface StoreQueryService {
    fun getAllStores(): Mono<List<StoreDTO>>
    fun getStoreById(storeId: Long): Mono<StoreDTO>
    fun getMenuByStoreId(storeId: Long): Mono<List<MenuItemDTO>>
}
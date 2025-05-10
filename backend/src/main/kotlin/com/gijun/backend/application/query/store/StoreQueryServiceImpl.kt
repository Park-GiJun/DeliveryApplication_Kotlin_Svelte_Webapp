package com.gijun.backend.application.query.store

import com.gijun.backend.application.dto.MenuItemDTO
import com.gijun.backend.application.dto.StoreDTO
import com.gijun.backend.domain.repository.store.StoreRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class StoreQueryServiceImpl(private val storeRepository: StoreRepository) : StoreQueryService {
    private val logger = LoggerFactory.getLogger(StoreQueryServiceImpl::class.java)

    override fun getAllStores(): Mono<List<StoreDTO>> {
        logger.info("모든 매장 정보 조회 서비스")
        return storeRepository.findAllStores()
    }

    override fun getStoreById(storeId: Long): Mono<StoreDTO> {
        logger.info("매장 정보 조회 서비스: storeId=$storeId")
        return storeRepository.findStoreById(storeId)
    }

    override fun getMenuByStoreId(storeId: Long): Mono<List<MenuItemDTO>> {
        logger.info("매장 메뉴 조회 서비스: storeId=$storeId")
        return storeRepository.findMenuByStoreId(storeId)
    }
}
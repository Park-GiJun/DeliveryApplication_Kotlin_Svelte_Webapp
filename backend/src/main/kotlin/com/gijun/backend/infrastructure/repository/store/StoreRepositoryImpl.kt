package com.gijun.backend.infrastructure.repository.store

import com.gijun.backend.application.dto.MenuItemDTO
import com.gijun.backend.application.dto.StoreDTO
import com.gijun.backend.domain.enums.store.ItemStatus
import com.gijun.backend.domain.model.store.MenuCategory
import com.gijun.backend.domain.model.store.MenuItem
import com.gijun.backend.domain.model.store.Store
import com.gijun.backend.domain.repository.store.StoreRepository
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class StoreRepositoryImpl(private val template: R2dbcEntityTemplate) : StoreRepository {
    private val logger = LoggerFactory.getLogger(StoreRepositoryImpl::class.java)

    override fun findAllStores(): Mono<List<StoreDTO>> {
        logger.info("모든 매장 조회 레포지토리")
        return template.select(Store::class.java)
            .all()
            .map { store ->
                StoreDTO(
                    id = store.id ?: 0,
                    name = store.name,
                    category = store.storeCategory.name,
                    address = "${store.address} ${store.detailAddress}",
                    status = store.storeStatus.name
                )
            }
            .collectList()
            .doOnNext { stores ->
                logger.info("조회된 매장 수: ${stores.size}")
            }
    }

    override fun findStoreById(storeId: Long): Mono<StoreDTO> {
        logger.info("매장 조회 레포지토리: storeId=$storeId")
        return template.select(Store::class.java)
            .matching(Query.query(where("id").`is`(storeId)))
            .one()
            .map { store ->
                StoreDTO(
                    id = store.id ?: 0,
                    name = store.name,
                    category = store.storeCategory.name,
                    address = "${store.address} ${store.detailAddress}",
                    status = store.storeStatus.name
                )
            }
    }

    override fun findMenuByStoreId(storeId: Long): Mono<List<MenuItemDTO>> {
        logger.info("매장 메뉴 조회 레포지토리: storeId=$storeId")

        return template.select(MenuCategory::class.java)
            .matching(Query.query(where("store_id").`is`(storeId)))
            .all()
            .flatMap { category ->
                if (category.id != null) {
                    template.select(MenuItem::class.java)
                        .matching(Query.query(where("category_id").`is`(category.id)))
                        .all()
                        .map { item ->
                            MenuItemDTO(
                                id = item.id ?: 0,
                                categoryId = item.categoryId,
                                categoryName = category.name,
                                name = item.name,
                                description = item.description ?: "",
                                price = item.price,
                                imageUrl = item.imageUrl,
                                available = item.itemStatus == ItemStatus.AVAILABLE
                            )
                        }
                } else {
                    reactor.core.publisher.Flux.empty()
                }
            }
            .collectList()
    }
}
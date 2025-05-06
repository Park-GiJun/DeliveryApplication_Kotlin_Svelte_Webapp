package com.gijun.backend.infrastructure.repository.store

import com.gijun.backend.domain.repository.store.StoreRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

class StoreRepositoryImpl(private val template: R2dbcEntityTemplate) : StoreRepository {
    private val logger = org.slf4j.LoggerFactory.getLogger(StoreRepositoryImpl::class.java)


}
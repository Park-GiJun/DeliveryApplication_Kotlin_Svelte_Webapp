package com.gijun.backend.infrastructure.repository.rider

import com.gijun.backend.domain.repository.rider.RiderRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

class RiderRepositoryImpl(private val template: R2dbcEntityTemplate) : RiderRepository {
    private val logger = org.slf4j.LoggerFactory.getLogger(RiderRepositoryImpl::class.java)
}
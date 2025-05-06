package com.gijun.backend.infrastructure.repository.delivery

import com.gijun.backend.domain.repository.delivery.DeliveryRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository

@Repository
class DeliveryRepositoryImpl (private val template: R2dbcEntityTemplate) : DeliveryRepository {
    private val logger = org.slf4j.LoggerFactory.getLogger(DeliveryRepositoryImpl::class.java)
}
package com.gijun.backend.infrastructure.repository.customer

import com.gijun.backend.domain.repository.customer.CustomerRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository

@Repository
class CustomerRepositoryImpl(private val template: R2dbcEntityTemplate) : CustomerRepository  {
    private val logger = org.slf4j.LoggerFactory.getLogger(CustomerRepositoryImpl::class.java)
}
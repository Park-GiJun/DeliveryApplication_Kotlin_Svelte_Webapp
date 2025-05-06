package com.gijun.backend.infrastructure.repository.customer

import com.gijun.backend.domain.model.customer.Customer
import com.gijun.backend.domain.repository.customer.CustomerRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class CustomerRepositoryImpl(private val template: R2dbcEntityTemplate) : CustomerRepository  {
    private val logger = org.slf4j.LoggerFactory.getLogger(CustomerRepositoryImpl::class.java)
    override fun findAllCustomers(): Mono<List<Customer>> {
        logger.info("모든 고객 검색")
        return template.select(Customer::class.java).all().collectList()
    }
}
package com.gijun.backend.domain.repository.customer

import com.gijun.backend.domain.model.customer.Customer
import reactor.core.publisher.Mono

interface CustomerRepository {
    fun findAllCustomers(): Mono<List<Customer>>
}
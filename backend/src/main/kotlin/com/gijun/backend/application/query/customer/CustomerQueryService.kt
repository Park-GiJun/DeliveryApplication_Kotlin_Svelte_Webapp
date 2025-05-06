package com.gijun.backend.application.query.customer

import com.gijun.backend.domain.model.customer.Customer
import reactor.core.publisher.Mono

interface CustomerQueryService {
    fun getAllCustomers(): Mono<List<Customer>>
}
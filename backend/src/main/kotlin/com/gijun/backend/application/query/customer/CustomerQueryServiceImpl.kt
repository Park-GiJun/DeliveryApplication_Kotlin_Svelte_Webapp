package com.gijun.backend.application.query.customer

import com.gijun.backend.domain.model.customer.Customer
import com.gijun.backend.domain.repository.customer.CustomerRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CustomerQueryServiceImpl(
    private val customerRepository: CustomerRepository
) : CustomerQueryService {
    override fun getAllCustomers(): Mono<List<Customer>> {
        return customerRepository.findAllCustomers()
    }
}
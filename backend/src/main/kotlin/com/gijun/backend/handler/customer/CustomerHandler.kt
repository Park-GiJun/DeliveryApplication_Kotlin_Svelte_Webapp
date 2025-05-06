package com.gijun.backend.handler.customer

import com.gijun.backend.application.query.customer.CustomerQueryService
import com.gijun.backend.domain.model.customer.Customer
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class CustomerHandler(private val customerQueryService: CustomerQueryService) {
    private val logger = org.slf4j.LoggerFactory.getLogger(CustomerHandler::class.java)

    fun getAllCustomers(request: ServerRequest): Mono<ServerResponse> {
        return customerQueryService.getAllCustomers()
            .flatMap { customers ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(customers)
            }
    }


}
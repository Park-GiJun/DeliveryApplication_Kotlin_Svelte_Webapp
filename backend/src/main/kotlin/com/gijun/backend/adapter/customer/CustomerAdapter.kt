package com.gijun.backend.adapter.customer

import com.gijun.backend.handler.customer.CustomerHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class CustomerAdapter(private val customerHandler: CustomerHandler) {
    private val logger = org.slf4j.LoggerFactory.getLogger(CustomerAdapter::class.java)

    @Bean
    fun customerRoutes(): RouterFunction<ServerResponse> {
        logger.info("고객 어댑터 라우터 초기화")
        return router {
            "/api/customers".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("/findAllCustomer", customerHandler::getAllCustomers)
                }
            }
        }
    }
}
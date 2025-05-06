package com.gijun.backend.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI 엔드포인트 그룹 설정
 */
@Configuration
class SpringDocConfig {

    @Bean
    fun allApis(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("all-apis")
            .pathsToMatch("/api/**")
            .displayName("모든 API")
            .build()
    }

    @Bean
    fun orderApis(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("orders-api")
            .pathsToMatch("/api/orders/**")
            .displayName("주문 API")
            .build()
    }

    @Bean
    fun connectionTestApis(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("connection-tests")
            .pathsToMatch("/api/connection-test/**")
            .displayName("연결 테스트 API")
            .build()
    }
}

package com.gijun.backend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(apiInfo())
            .components(Components())
            .addServersItem(Server().url("/").description("기본 서버"))
    }

    private fun apiInfo() = Info()
        .title("배달 시뮬레이터 API")
        .description("실시간 배달 매칭 시뮬레이션 시스템의 REST API 문서")
        .version("v1.0.0")
        .contact(
            Contact()
                .name("개발팀")
                .email("dev@example.com")
        )
        .license(
            License()
                .name("Apache License Version 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0")
        )
}

package com.gijun.backend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger/OpenAPI 문서화 설정
 */
@Configuration
class SwaggerConfig(
    @Value("\${springdoc.version:1.0.0}") private val appVersion: String,
    @Value("\${spring.application.name:배달 시뮬레이터}") private val appName: String,
    @Value("\${server.port:52001}") private val serverPort: String
) {
    private val logger = LoggerFactory.getLogger(SwaggerConfig::class.java)

    /**
     * OpenAPI 기본 설정
     */
    @Bean
    fun openAPI(): OpenAPI {
        logger.info("Swagger/OpenAPI 설정: 애플리케이션={}, 버전={}", appName, appVersion)
        
        return OpenAPI()
            .info(apiInfo())
            .servers(listOf(
                Server().url("http://localhost:$serverPort").description("로컬 서버"),
                Server().url("https://api.delivery-simulator.com").description("운영 서버")
            ))
            .components(
                Components()
                    .addSecuritySchemes("bearer-jwt", 
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT 인증 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다.")
                    )
            )
    }

    /**
     * API 정보 설정
     */
    private fun apiInfo(): Info {
        return Info()
            .title("$appName API")
            .description("배달 시뮬레이터 API 문서: 배달 주문, 라이더, 매장 관련 API를 제공합니다.")
            .version(appVersion)
            .contact(
                Contact()
                    .name("API 개발팀")
                    .email("api@delivery-simulator.com")
                    .url("https://delivery-simulator.com/docs")
            )
            .license(
                License()
                    .name("Apache 2.0")
                    .url("http://www.apache.org/licenses/LICENSE-2.0.html")
            )
            .termsOfService("https://delivery-simulator.com/terms")
    }

    /**
     * 주문 API 그룹 설정
     */
    @Bean
    fun orderApiGroup(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("1. 주문 API")
            .pathsToMatch("/api/orders/**")
            .displayName("주문 API")
            .addOpenApiCustomizer(orderApiCustomizer())
            .build()
    }

    /**
     * 매장 API 그룹 설정
     */
    @Bean
    fun storeApiGroup(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("2. 매장 API")
            .pathsToMatch("/api/stores/**")
            .displayName("매장 API")
            .build()
    }

    /**
     * 라이더 API 그룹 설정
     */
    @Bean
    fun riderApiGroup(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("3. 라이더 API")
            .pathsToMatch("/api/riders/**")
            .displayName("라이더 API")
            .build()
    }

    /**
     * 고객 API 그룹 설정
     */
    @Bean
    fun customerApiGroup(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("4. 고객 API")
            .pathsToMatch("/api/customers/**")
            .displayName("고객 API")
            .build()
    }

    /**
     * 테스트 및 모니터링 API 그룹 설정
     */
    @Bean
    fun testApiGroup(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("5. 테스트 및 모니터링 API")
            .pathsToMatch("/api/connection-test/**", "/api/status/**")
            .displayName("테스트 및 모니터링 API")
            .build()
    }

    /**
     * 주문 API 커스터마이저 - 주문 API에 특화된 설명 추가
     */
    private fun orderApiCustomizer(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi ->
            openApi.paths.forEach { (path, pathItem) ->
                if (path.startsWith("/api/orders")) {
                    // 주문 생성 API에 대한 설명 추가
                    if (path == "/api/orders" && pathItem.post != null) {
                        pathItem.post.description = "새로운 주문을 생성합니다. 주문 항목 및 옵션을 포함한 상세 정보가 필요합니다."
                    }
                    
                    // 주문 조회 API에 대한 설명 추가
                    if (path.matches(Regex("/api/orders/[^/]+")) && pathItem.get != null) {
                        pathItem.get.description = "주문 번호로 주문 상세 정보를 조회합니다. 상품 정보와 주문 상태가 포함됩니다."
                    }
                    
                    // 주문 취소 API에 대한 설명 추가
                    if (path.matches(Regex("/api/orders/[^/]+/cancel")) && pathItem.post != null) {
                        pathItem.post.description = "주문을 취소합니다. 취소 사유를 본문에 포함할 수 있습니다."
                    }
                    
                    // 고객별 주문 목록 조회 API에 대한 설명 추가
                    if (path.matches(Regex("/api/orders/customer/[^/]+")) && pathItem.get != null) {
                        pathItem.get.description = "고객 ID로 주문 목록을 조회합니다. 페이징을 지원합니다."
                    }
                }
            }
        }
    }
}

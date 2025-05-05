package com.gijun.backend.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class WebFluxConfig : WebFluxConfigurer {
    private val logger = LoggerFactory.getLogger(WebFluxConfig::class.java)

    // CORS 설정 (프론트엔드 개발 환경과의 통신을 위함)
    override fun addCorsMappings(registry: CorsRegistry) {
        logger.info("CORS 설정 적용: 모든 오리진 허용")
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
    }
}
package com.gijun.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

/**
 * 함수형 엔드포인트 문서화 지원을 위한 설정
 */
@Configuration
@EnableWebFlux
class RouterFunctionConfig : WebFluxConfigurer {
    // SpringDoc은 함수형 엔드포인트를 자동으로 감지하므로 
    // 여기서는 어떤 명시적인 설정도 필요하지 않습니다.
    // 대신 각 Adapter 클래스에 @RouterOperation 어노테이션을 추가했습니다.
}

package com.gijun.backend.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@ConditionalOnProperty(value = ["spring.data.redis.enabled"], havingValue = "true", matchIfMissing = true)
class RedisConfig(
    @Value("\${spring.data.redis.host:localhost}") private val redisHost: String,
    @Value("\${spring.data.redis.port:6379}") private val redisPort: Int,
    @Value("\${spring.data.redis.username:}") private val redisUsername: String,
    @Value("\${spring.data.redis.password:}") private val redisPassword: String
) {
    private val logger = LoggerFactory.getLogger(RedisConfig::class.java)

    @Bean
    @Primary
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {
        val configuration = RedisStandaloneConfiguration(redisHost, redisPort)
        
        // 환경 변수에서 가져온 인증 정보 설정
        if (redisUsername.isNotBlank()) {
            configuration.username = redisUsername
        }
        
        if (redisPassword.isNotBlank()) {
            configuration.password = RedisPassword.of(redisPassword)
        }
        
        logger.info("Redis 연결 설정: 호스트=${redisHost}, 포트=${redisPort}, 사용자=${redisUsername}")
        
        val factory = LettuceConnectionFactory(configuration)
        // 연결 실패해도 계속 진행
        factory.setValidateConnection(false)
        return factory
    }

    @Bean
    @Primary
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, String>(StringRedisSerializer())
            .build()
            
        return ReactiveRedisTemplate(factory, serializationContext)
    }
}

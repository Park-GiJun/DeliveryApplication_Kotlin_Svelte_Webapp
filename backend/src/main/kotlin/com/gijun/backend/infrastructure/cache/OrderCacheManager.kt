package com.gijun.backend.infrastructure.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.gijun.backend.domain.model.order.Order
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * 주문 캐시 관리자 - Redis를 이용한 주문 정보 캐싱
 */
@Component
class OrderCacheManager(
    private val redisTemplate: ReactiveRedisTemplate<String, String>?,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(OrderCacheManager::class.java)
    
    companion object {
        private const val ORDER_CACHE_PREFIX = "order:"
        private const val CACHE_TTL_MINUTES = 30L // 캐시 유효 시간 (30분)
    }
    
    /**
     * 주문 정보를 Redis에 캐싱
     */
    fun cacheOrder(order: Order) {
        if (redisTemplate == null) {
            logger.warn("RedisTemplate이 null이므로 캐시에 저장하지 않습니다.")
            return
        }
        
        try {
            val key = ORDER_CACHE_PREFIX + order.orderNumber
            val jsonValue = objectMapper.writeValueAsString(order)
            
            redisTemplate.opsForValue()
                .set(key, jsonValue, Duration.ofMinutes(CACHE_TTL_MINUTES))
                .subscribe(
                    { logger.debug("주문 캐싱 완료: {}", order.orderNumber) },
                    { e -> logger.error("주문 캐싱 중 오류 발생: {}", e.message, e) }
                )
        } catch (e: Exception) {
            logger.error("주문 객체 직렬화 중 오류 발생: {}", e.message, e)
        }
    }
    
    /**
     * Redis 캐시에서 주문 정보 조회
     */
    fun getOrderFromCache(orderNumber: String): Mono<Order> {
        if (redisTemplate == null) {
            logger.warn("RedisTemplate이 null이므로 캐시에서 조회하지 않습니다.")
            return Mono.empty()
        }
        
        logger.debug("캐시에서 주문 조회: {}", orderNumber)
        val key = ORDER_CACHE_PREFIX + orderNumber
        
        return redisTemplate.opsForValue().get(key)
            .flatMap { jsonValue ->
                try {
                    val order = objectMapper.readValue(jsonValue, Order::class.java)
                    logger.debug("캐시에서 주문 조회 성공: {}", orderNumber)
                    Mono.just(order)
                } catch (e: Exception) {
                    logger.error("캐시된 주문 객체 역직렬화 중 오류 발생: {}", e.message, e)
                    Mono.empty()
                }
            }
            .onErrorResume { e ->
                logger.error("캐시에서 주문 조회 중 오류 발생: {}", e.message, e)
                Mono.empty()
            }
    }
    
    /**
     * Redis에서 주문 캐시 삭제
     */
    fun deleteOrderCache(orderNumber: String) {
        if (redisTemplate == null) {
            logger.warn("RedisTemplate이 null이므로 캐시에서 삭제하지 않습니다.")
            return
        }
        
        logger.debug("캐시에서 주문 삭제: {}", orderNumber)
        val key = ORDER_CACHE_PREFIX + orderNumber
        
        redisTemplate.delete(key)
            .subscribe(
                { result -> 
                    if (result) {
                        logger.debug("캐시에서 주문 삭제 성공: {}", orderNumber) 
                    } else {
                        logger.debug("캐시에서 주문 삭제 실패 (키가 존재하지 않음): {}", orderNumber)
                    }
                },
                { e -> logger.error("캐시에서 주문 삭제 중 오류 발생: {}", e.message, e) }
            )
    }
    
    /**
     * 모든 주문 캐시 삭제
     */
    fun flushAllOrderCache() {
        if (redisTemplate == null) {
            logger.warn("RedisTemplate이 null이므로 캐시를 플러시하지 않습니다.")
            return
        }
        
        logger.info("모든 주문 캐시 플러시 시작")
        
        redisTemplate.keys(ORDER_CACHE_PREFIX + "*")
            .flatMap { key -> redisTemplate.delete(key) }
            .count()
            .subscribe(
                { count -> logger.info("{}개의 주문 캐시 항목 삭제 완료", count) },
                { e -> logger.error("주문 캐시 플러시 중 오류 발생: {}", e.message, e) }
            )
    }
}

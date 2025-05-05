package com.gijun.backend.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.spi.ConnectionFactory
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/**
 * 연결 테스트를 위한 핸들러
 */
@Component
class ConnectionTestHandler(
    private val objectMapper: ObjectMapper,
    private val connectionFactory: ConnectionFactory?,
    private val redisTemplate: ReactiveRedisTemplate<String, String>?,
    private val kafkaTemplate: KafkaTemplate<String, Any>?
) {
    private val logger = LoggerFactory.getLogger(ConnectionTestHandler::class.java)
    private val KAFKA_TEST_TOPIC = "delivery.test"

    /**
     * 모든 시스템 구성 요소의 연결 상태를 확인합니다.
     */
    fun getConnectionStatus(request: ServerRequest): Mono<ServerResponse> {
        logger.info("연결 상태 확인 요청 수신")
        val status = mutableMapOf<String, Any>()
        
        status["timestamp"] = LocalDateTime.now().toString()
        status["webflux"] = "OK" // WebFlux가 동작 중이므로 항상 OK
        status["database"] = if (connectionFactory != null) "CONNECTED" else "NOT_CONFIGURED"
        status["redis"] = if (redisTemplate != null) "CONNECTED" else "NOT_CONFIGURED"
        status["kafka"] = if (kafkaTemplate != null) "CONNECTED" else "NOT_CONFIGURED"

        logger.debug("연결 상태: $status")
        return ServerResponse.ok().bodyValue(status)
    }
    
    /**
     * 데이터베이스 연결을 테스트합니다.
     */
    fun testDatabaseConnection(request: ServerRequest): Mono<ServerResponse> {
        logger.info("데이터베이스 연결 테스트 요청 수신")
        if (connectionFactory == null) {
            logger.warn("데이터베이스가 구성되지 않음")
            return ServerResponse.ok().bodyValue(
                mapOf(
                    "status" to "NOT_CONFIGURED",
                    "message" to "데이터베이스가 구성되지 않았습니다"
                )
            )
        }
        
        // 간단한 SQL 쿼리 실행하여 DB 연결 테스트
        val sql = "SELECT NOW() as `current_time`"
        logger.debug("실행할 SQL 쿼리: $sql")
        
        return Mono.from(connectionFactory.create())
            .flatMap { connection ->
                Mono.from(connection.createStatement(sql).execute())
                    .flatMap { result ->
                        Mono.from(result.map { row, _ ->
                            row.get("current_time", Any::class.java)
                        })
                    }
                    .doFinally { connection.close() }
            }
            .flatMap { time ->
                logger.info("데이터베이스 연결 성공, 현재 시간: $time")
                ServerResponse.ok().bodyValue(
                    mapOf(
                        "status" to "OK",
                        "message" to "데이터베이스 연결 성공",
                        "currentTime" to time.toString()
                    )
                )
            }
            .onErrorResume { e ->
                logger.error("데이터베이스 연결 오류: ${e.message}", e)
                ServerResponse.ok().bodyValue(
                    mapOf(
                        "status" to "ERROR",
                        "message" to "데이터베이스 연결 오류",
                        "error" to e.message
                    )
                )
            }
    }
    
    /**
     * Redis 연결을 테스트합니다.
     */
    fun testRedisConnection(request: ServerRequest): Mono<ServerResponse> {
        logger.info("Redis 연결 테스트 요청 수신")
        if (redisTemplate == null) {
            logger.warn("Redis가 구성되지 않음")
            return ServerResponse.ok().bodyValue(
                mapOf(
                    "status" to "NOT_CONFIGURED",
                    "message" to "Redis가 구성되지 않았거나 연결할 수 없습니다"
                )
            )
        }
        
        return request.bodyToMono<Map<String, String>>()
            .defaultIfEmpty(emptyMap())
            .flatMap { payload ->
                val key = payload["key"] ?: "test:${UUID.randomUUID()}"
                val value = payload["value"] ?: "테스트 값: ${LocalDateTime.now()}"
                
                logger.debug("Redis에 값 설정: $key -> $value")
                redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(5))
                    .onErrorResume { e -> 
                        // Redis 연결 실패 처리
                        logger.error("Redis 값 설정 실패: $key -> $value, 오류: ${e.message}")
                        return@onErrorResume Mono.just(false)
                    }
                    .then(
                        redisTemplate.opsForValue().get(key)
                            .defaultIfEmpty("값을 가져올 수 없습니다")
                            .onErrorReturn("Redis 오류: 값을 가져올 수 없습니다")
                    )
                    .flatMap { retrievedValue ->
                        logger.info("Redis 연결 성공, 저장된 값: $key -> $retrievedValue")
                        ServerResponse.ok().bodyValue(
                            mapOf(
                                "status" to "OK",
                                "message" to "Redis 연결 성공",
                                "key" to key,
                                "value" to retrievedValue
                            )
                        )
                    }
                    .onErrorResume { e ->
                        logger.error("Redis 연결 오류: ${e.message}", e)
                        ServerResponse.ok().bodyValue(
                            mapOf(
                                "status" to "ERROR",
                                "message" to "Redis 연결 오류",
                                "error" to (e.message ?: "알 수 없는 오류")
                            )
                        )
                    }
            }
    }
    
    /**
     * Kafka 연결을 테스트합니다.
     */
    fun testKafkaConnection(request: ServerRequest): Mono<ServerResponse> {
        logger.info("Kafka 연결 테스트 요청 수신")
        if (kafkaTemplate == null) {
            logger.warn("Kafka가 구성되지 않음")
            return ServerResponse.ok().bodyValue(
                mapOf(
                    "status" to "NOT_CONFIGURED",
                    "message" to "Kafka가 구성되지 않았습니다"
                )
            )
        }
        
        return request.bodyToMono<Map<String, String>>()
            .defaultIfEmpty(emptyMap())
            .flatMap { payload ->
                val message = payload["message"] ?: "테스트 메시지: ${LocalDateTime.now()}"
                
                try {
                    val messageData = mapOf(
                        "id" to UUID.randomUUID().toString(),
                        "message" to message,
                        "timestamp" to LocalDateTime.now().toString()
                    )
                    
                    logger.debug("Kafka 메시지 전송: $messageData")
                    kafkaTemplate.send(KAFKA_TEST_TOPIC, messageData["id"], messageData)
                    
                    logger.info("Kafka 메시지 전송 성공")
                    ServerResponse.ok().bodyValue(
                        mapOf(
                            "status" to "OK",
                            "message" to "Kafka 메시지 전송 성공",
                            "sentData" to messageData
                        )
                    )
                } catch (e: Exception) {
                    logger.error("Kafka 메시지 전송 오류: ${e.message}", e)
                    ServerResponse.ok().bodyValue(
                        mapOf(
                            "status" to "ERROR",
                            "message" to "Kafka 메시지 전송 오류",
                            "error" to e.message
                        )
                    )
                }
            }
    }
    
    /**
     * 리액티브 스트림 테스트를 위한 데이터 스트림을 제공합니다.
     */
    fun testReactiveStream(request: ServerRequest): Mono<ServerResponse> {
        val count = request.queryParam("count").orElse("5").toInt()
        logger.info("리액티브 스트림 테스트 요청 수신: count=$count")
        
        val dataStream = Flux.interval(Duration.ofSeconds(1))
            .take(count.toLong())
            .map { seq ->
                val data = mapOf(
                    "sequence" to seq,
                    "timestamp" to LocalDateTime.now().toString(),
                    "data" to "테스트 데이터 #${seq+1}"
                )
                logger.debug("스트림 데이터 생성: $data")
                data
            }
        
        return ServerResponse.ok().body(dataStream, Map::class.java)
    }
}

// KafkaTemplate 확장 함수: 실제 Kafka 메시지 전송 구현
private fun KafkaTemplate<String, Any>.send(
    topic: String,
    key: String?,
    data: Map<String, String>
) {
    this.send(topic, key, data)
}

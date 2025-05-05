package com.gijun.backend.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.spi.ConnectionFactory
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
 * 연결 테스트를 위한 핸들러 어댑터
 */
@Component
class ConnectionTestHandler(
    private val objectMapper: ObjectMapper,
    private val connectionFactory: ConnectionFactory?,
    private val redisTemplate: ReactiveRedisTemplate<String, String>?,
    private val kafkaTemplate: KafkaTemplate<String, Any>?
) {
    private val KAFKA_TEST_TOPIC = "delivery.test"

    /**
     * 모든 시스템 구성 요소의 연결 상태를 확인합니다.
     */
    fun getConnectionStatus(request: ServerRequest): Mono<ServerResponse> {
        val status = mutableMapOf<String, Any>()
        
        status["timestamp"] = LocalDateTime.now().toString()
        status["webflux"] = "OK" // WebFlux가 동작 중이므로 항상 OK
        status["database"] = if (connectionFactory != null) "CONNECTED" else "NOT_CONFIGURED"
        status["redis"] = if (redisTemplate != null) "CONNECTED" else "NOT_CONFIGURED"
        status["kafka"] = if (kafkaTemplate != null) "CONNECTED" else "NOT_CONFIGURED"

        return ServerResponse.ok().bodyValue(status)
    }
    
    /**
     * 데이터베이스 연결을 테스트합니다.
     */
    fun testDatabaseConnection(request: ServerRequest): Mono<ServerResponse> {
        if (connectionFactory == null) {
            return ServerResponse.ok().bodyValue(
                mapOf(
                    "status" to "NOT_CONFIGURED",
                    "message" to "데이터베이스가 구성되지 않았습니다"
                )
            )
        }
        
        // 간단한 SQL 쿼리 실행하여 DB 연결 테스트
        val sql = "SELECT NOW() as current_time"
        
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
                ServerResponse.ok().bodyValue(
                    mapOf(
                        "status" to "OK",
                        "message" to "데이터베이스 연결 성공",
                        "currentTime" to time.toString()
                    )
                )
            }
            .onErrorResume { e ->
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
        if (redisTemplate == null) {
            return ServerResponse.ok().bodyValue(
                mapOf(
                    "status" to "NOT_CONFIGURED",
                    "message" to "Redis가 구성되지 않았습니다"
                )
            )
        }
        
        return request.bodyToMono<Map<String, String>>()
            .defaultIfEmpty(emptyMap())
            .flatMap { payload ->
                val key = payload["key"] ?: "test:${UUID.randomUUID()}"
                val value = payload["value"] ?: "테스트 값: ${LocalDateTime.now()}"
                
                redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(5))
                    .then(redisTemplate.opsForValue().get(key))
                    .flatMap { retrievedValue ->
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
                        ServerResponse.ok().bodyValue(
                            mapOf(
                                "status" to "ERROR",
                                "message" to "Redis 연결 오류",
                                "error" to e.message
                            )
                        )
                    }
            }
    }
    
    /**
     * Kafka 연결을 테스트합니다.
     */
    fun testKafkaConnection(request: ServerRequest): Mono<ServerResponse> {
        if (kafkaTemplate == null) {
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
                    
                    kafkaTemplate.send(KAFKA_TEST_TOPIC, messageData["id"], messageData)
                    
                    ServerResponse.ok().bodyValue(
                        mapOf(
                            "status" to "OK",
                            "message" to "Kafka 메시지 전송 성공",
                            "sentData" to messageData
                        )
                    )
                } catch (e: Exception) {
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
        
        val dataStream = Flux.interval(Duration.ofSeconds(1))
            .take(count.toLong())
            .map { seq ->
                mapOf(
                    "sequence" to seq,
                    "timestamp" to LocalDateTime.now().toString(),
                    "data" to "테스트 데이터 #${seq+1}"
                )
            }
        
        return ServerResponse.ok().body(dataStream, Map::class.java)
    }
}

private fun KafkaTemplate<String, Any>.send(
    topic: String,
    key: String?,
    data: Map<String, String>
) {
}

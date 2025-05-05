package com.gijun.backend.handler

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 에코 웹소켓 핸들러 - 클라이언트의 메시지를 그대로 반환합니다.
 */
class EchoWebSocketHandler : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(EchoWebSocketHandler::class.java)

    override fun handle(session: WebSocketSession): Mono<Void> {
        logger.info("에코 WebSocket 세션 연결됨: ID=${session.id}")
        
        return session.send(
            session.receive()
                .map { message -> 
                    val payload = message.payloadAsText
                    logger.debug("에코 메시지 수신: $payload")
                    session.textMessage("에코: $payload")
                }
        ).doFinally {
            logger.info("에코 WebSocket 세션 종료됨: ID=${session.id}")
        }
    }
}

/**
 * 이벤트 스트림 웹소켓 핸들러 - 주기적으로 이벤트 데이터를 푸시합니다.
 */
class EventStreamWebSocketHandler(private val objectMapper: ObjectMapper) : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(EventStreamWebSocketHandler::class.java)
    
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val sink = Sinks.many().multicast().onBackpressureBuffer<Map<String, Any>>()
    
    init {
        logger.info("이벤트 스트림 웹소켓 핸들러 초기화 - 3초마다 테스트 이벤트 생성")
        
        // 3초마다 테스트 이벤트 생성
        Flux.interval(Duration.ofSeconds(3))
            .map { _ ->
                val eventType = listOf("INFO", "UPDATE", "ALERT").random()
                mapOf(
                    "id" to UUID.randomUUID().toString(),
                    "type" to eventType,
                    "timestamp" to LocalDateTime.now().toString(),
                    "message" to "테스트 이벤트 메시지: $eventType"
                )
            }
            .subscribe { event ->
                logger.debug("테스트 이벤트 생성: $event") 
                sink.tryEmitNext(event)
            }
    }
    
    override fun handle(session: WebSocketSession): Mono<Void> {
        logger.info("WebSocket 세션 연결됨: ID=${session.id}")
        sessions[session.id] = session
        
        val output = sink.asFlux()
            .map { event ->
                try {
                    val json = objectMapper.writeValueAsString(event)
                    session.textMessage(json)
                } catch (e: Exception) {
                    logger.error("이벤트 직렬화 오류: ${e.message}", e)
                    session.textMessage("{\"error\": \"직렬화 오류\", \"message\": \"${e.message}\"}")
                }
            }
        
        return session.send(output)
            .doFinally {
                logger.info("WebSocket 세션 종료됨: ID=${session.id}")
                sessions.remove(session.id)
            }
    }
    
    // 외부에서 이벤트를 발생시키기 위한 메서드
    fun sendEvent(event: Map<String, Any>) {
        logger.debug("이벤트 발생: $event")
        sink.tryEmitNext(event)
    }
}

package com.gijun.backend.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Configuration
class WebSocketConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }

    @Bean
    fun webSocketHandlerMapping(
        echoHandler: EchoWebSocketHandler,
        eventStreamHandler: EventStreamWebSocketHandler
    ): HandlerMapping {
        val map = HashMap<String, WebSocketHandler>()
        map["/ws/echo"] = echoHandler
        map["/ws/events"] = eventStreamHandler
        
        val handlerMapping = SimpleUrlHandlerMapping()
        handlerMapping.urlMap = map
        handlerMapping.order = 1
        return handlerMapping
    }
    
    @Bean
    fun handlerAdapter() = WebSocketHandlerAdapter(ReactorNettyRequestUpgradeStrategy() as WebSocketService)
    
    @Bean
    fun echoWebSocketHandler() = EchoWebSocketHandler()
    
    @Bean
    fun eventStreamWebSocketHandler(objectMapper: ObjectMapper) = EventStreamWebSocketHandler(objectMapper)
}

/**
 * 에코 웹소켓 핸들러 - 클라이언트의 메시지를 그대로 반환합니다.
 */
class EchoWebSocketHandler : WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> {
        return session.send(
            session.receive()
                .map { message -> 
                    val payload = message.payloadAsText
                    session.textMessage("에코: $payload")
                }
        )
    }
}

/**
 * 이벤트 스트림 웹소켓 핸들러 - 주기적으로 이벤트 데이터를 푸시합니다.
 */
class EventStreamWebSocketHandler(private val objectMapper: ObjectMapper) : WebSocketHandler {
    
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val sink = Sinks.many().multicast().onBackpressureBuffer<Map<String, Any>>()
    
    init {
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
            .subscribe { event -> sink.tryEmitNext(event) }
    }
    
    override fun handle(session: WebSocketSession): Mono<Void> {
        sessions[session.id] = session
        
        val output = sink.asFlux()
            .map { event ->
                try {
                    val json = objectMapper.writeValueAsString(event)
                    session.textMessage(json)
                } catch (e: Exception) {
                    session.textMessage("{\"error\": \"직렬화 오류\", \"message\": \"${e.message}\"}")
                }
            }
        
        return session.send(output)
            .doFinally { sessions.remove(session.id) }
    }
    
    // 외부에서 이벤트를 발생시키기 위한 메서드
    fun sendEvent(event: Map<String, Any>) {
        sink.tryEmitNext(event)
    }
}

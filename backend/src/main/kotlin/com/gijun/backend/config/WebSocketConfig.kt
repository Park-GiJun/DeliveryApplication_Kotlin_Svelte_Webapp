package com.gijun.backend.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.gijun.backend.handler.EchoWebSocketHandler
import com.gijun.backend.handler.EventStreamWebSocketHandler
import com.gijun.backend.handler.StoreNotificationWebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy

@Configuration
class WebSocketConfig {
    private val logger = LoggerFactory.getLogger(WebSocketConfig::class.java)

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }

    @Bean
    fun webSocketHandlerMapping(
        echoHandler: EchoWebSocketHandler,
        eventStreamHandler: EventStreamWebSocketHandler,
        storeNotificationHandler: StoreNotificationWebSocketHandler
    ): HandlerMapping {
        val map = HashMap<String, WebSocketHandler>()
        map["/ws/echo"] = echoHandler
        map["/ws/events"] = eventStreamHandler
        map["/ws/store/{storeId}"] = storeNotificationHandler
        
        logger.info("WebSocket 핸들러 매핑 설정: ${map.keys}")
        
        val handlerMapping = SimpleUrlHandlerMapping()
        handlerMapping.urlMap = map
        handlerMapping.order = 1
        return handlerMapping
    }
    
    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        val webSocketService = HandshakeWebSocketService(ReactorNettyRequestUpgradeStrategy())
        return WebSocketHandlerAdapter(webSocketService)
    }
    
    @Bean
    fun echoWebSocketHandler() = EchoWebSocketHandler()
    
    @Bean
    fun eventStreamWebSocketHandler(objectMapper: ObjectMapper) = EventStreamWebSocketHandler(objectMapper)
}

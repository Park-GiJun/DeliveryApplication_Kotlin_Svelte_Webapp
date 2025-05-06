package com.gijun.backend.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.gijun.backend.handler.CustomerNotificationWebSocketHandler
import com.gijun.backend.handler.EchoWebSocketHandler
import com.gijun.backend.handler.EventStreamWebSocketHandler
import com.gijun.backend.handler.RiderNotificationWebSocketHandler
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
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        return objectMapper
    }


    @Bean
    fun webSocketHandlerMapping(
        echoHandler: EchoWebSocketHandler,
        eventStreamHandler: EventStreamWebSocketHandler,
        storeNotificationHandler: StoreNotificationWebSocketHandler,
        riderNotificationHandler: RiderNotificationWebSocketHandler,
        customerNotificationHandler: CustomerNotificationWebSocketHandler
    ): HandlerMapping {
        val map = HashMap<String, WebSocketHandler>()
        map["/ws/echo"] = echoHandler
        map["/ws/events"] = eventStreamHandler
        map["/ws/store/{storeId}"] = storeNotificationHandler
        map["/ws/rider/{riderId}"] = riderNotificationHandler
        map["/ws/customer/{customerId}"] = customerNotificationHandler

        
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
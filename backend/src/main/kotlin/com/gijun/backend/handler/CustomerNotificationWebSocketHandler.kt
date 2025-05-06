package com.gijun.backend.handler

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * 고객 알림 웹소켓 핸들러 - 고객 별로 주문 상태 변경 알림 등을 전송합니다.
 */
@Component
class CustomerNotificationWebSocketHandler(private val objectMapper: ObjectMapper) : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(CustomerNotificationWebSocketHandler::class.java)
    
    // 고객 ID를 키로 하는 세션 맵
    private val customerSessionsMap = ConcurrentHashMap<Long, ConcurrentHashMap<String, WebSocketSession>>()
    
    // 고객 ID를 키로 하는 싱크 맵
    private val customerSinksMap = ConcurrentHashMap<Long, Sinks.Many<Map<String, Any>>>()
    
    override fun handle(session: WebSocketSession): Mono<Void> {
        // 경로에서 고객 ID 추출 (예: /ws/customer/1)
        val path = session.handshakeInfo.uri.path
        val customerId = extractCustomerId(path)
        
        if (customerId == null) {
            logger.error("잘못된 웹소켓 경로: $path")
            return session.close()
        }
        
        logger.info("고객 $customerId의 WebSocket 세션 연결됨: ID=${session.id}")
        
        // 고객 ID에 해당하는 세션 맵 가져오기
        val customerSessions = customerSessionsMap.computeIfAbsent(customerId) { ConcurrentHashMap() }
        customerSessions[session.id] = session
        
        // 고객 ID에 해당하는 싱크 가져오기
        val customerSink = customerSinksMap.computeIfAbsent(customerId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }
        
        // 웰컴 메시지 전송
        sendWelcomeMessage(customerId, session)
        
        // 세션에 싱크의 출력 연결
        val output = customerSink.asFlux()
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
            .doFinally { signal ->
                logger.info("고객 $customerId의 WebSocket 세션 종료됨: ID=${session.id}, signal=$signal")
                customerSessions.remove(session.id)
                
                // 모든 세션이 종료되면 싱크도 제거
                if (customerSessions.isEmpty()) {
                    customerSinksMap.remove(customerId)
                    logger.info("고객 $customerId의 모든 세션이 종료되어 싱크를 제거합니다.")
                }
            }
    }
    
    /**
     * 경로에서 고객 ID를 추출합니다.
     * 예: /ws/customer/1 -> 1
     */
    private fun extractCustomerId(path: String): Long? {
        val regex = "/ws/customer/(\\d+)".toRegex()
        val matchResult = regex.find(path)
        
        return matchResult?.groupValues?.get(1)?.toLongOrNull()
    }
    
    /**
     * 웰컴 메시지를 전송합니다.
     */
    private fun sendWelcomeMessage(customerId: Long, session: WebSocketSession) {
        val welcomeMessage = mapOf(
            "eventType" to "WELCOME",
            "customerId" to customerId,
            "timestamp" to LocalDateTime.now().toString(),
            "message" to "고객 웹소켓 서버에 연결되었습니다."
        )
        
        try {
            val json = objectMapper.writeValueAsString(welcomeMessage)
            session.send(Mono.just(session.textMessage(json))).subscribe()
        } catch (e: Exception) {
            logger.error("웰컴 메시지 전송 오류: ${e.message}", e)
        }
    }
    
    /**
     * 특정 고객에 이벤트를 전송합니다.
     */
    fun sendEventToCustomer(customerId: Long, event: Map<String, Any>) {
        logger.debug("고객 $customerId에 이벤트 전송: $event")
        
        val sink = customerSinksMap[customerId]
        if (sink != null) {
            sink.tryEmitNext(event)
            logger.debug("고객 $customerId에 이벤트 전송 성공")
        } else {
            logger.warn("고객 $customerId에 연결된 세션이 없습니다.")
        }
    }
    
    /**
     * 여러 고객에 이벤트를 브로드캐스트합니다.
     */
    fun broadcastEvent(customerIds: List<Long>, event: Map<String, Any>) {
        logger.debug("${customerIds.size}명의 고객에 이벤트 브로드캐스트: $event")
        
        customerIds.forEach { customerId ->
            sendEventToCustomer(customerId, event)
        }
    }
    
    /**
     * 모든 고객에 이벤트를 브로드캐스트합니다.
     */
    fun broadcastToAll(event: Map<String, Any>) {
        logger.debug("모든 고객에 이벤트 브로드캐스트: $event")
        
        customerSessionsMap.keys.forEach { customerId ->
            sendEventToCustomer(customerId, event)
        }
    }
    
    /**
     * 특정 주문에 대한 상태 변경 이벤트를 전송합니다.
     * (주문 객체에서 고객 ID를 추출)
     */
    fun sendOrderStatusUpdate(orderData: Map<String, Any>, eventType: String) {
        val customerId = orderData["customerId"] as? Long ?: return
        val orderNumber = orderData["orderNumber"] as? String ?: return
        
        val event = mapOf(
            "eventType" to eventType,
            "orderNumber" to orderNumber,
            "timestamp" to LocalDateTime.now().toString(),
            "data" to orderData
        )
        
        sendEventToCustomer(customerId, event)
    }
}
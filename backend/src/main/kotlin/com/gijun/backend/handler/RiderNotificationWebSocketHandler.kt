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
 * 라이더 알림 웹소켓 핸들러 - 라이더 별로 배달 알림 등을 전송합니다.
 */
@Component
class RiderNotificationWebSocketHandler(private val objectMapper: ObjectMapper) : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(RiderNotificationWebSocketHandler::class.java)
    
    // 라이더 ID를 키로 하는 세션 맵
    private val riderSessionsMap = ConcurrentHashMap<Long, ConcurrentHashMap<String, WebSocketSession>>()
    
    // 라이더 ID를 키로 하는 싱크 맵
    private val riderSinksMap = ConcurrentHashMap<Long, Sinks.Many<Map<String, Any>>>()
    
    override fun handle(session: WebSocketSession): Mono<Void> {
        // 경로에서 라이더 ID 추출 (예: /ws/rider/1)
        val path = session.handshakeInfo.uri.path
        val riderId = extractRiderId(path)
        
        if (riderId == null) {
            logger.error("잘못된 웹소켓 경로: $path")
            return session.close()
        }
        
        logger.info("라이더 $riderId의 WebSocket 세션 연결됨: ID=${session.id}")
        
        // 라이더 ID에 해당하는 세션 맵 가져오기
        val riderSessions = riderSessionsMap.computeIfAbsent(riderId) { ConcurrentHashMap() }
        riderSessions[session.id] = session
        
        // 라이더 ID에 해당하는 싱크 가져오기
        val riderSink = riderSinksMap.computeIfAbsent(riderId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }
        
        // 웰컴 메시지 전송
        sendWelcomeMessage(riderId, session)
        
        // 세션에 싱크의 출력 연결
        val output = riderSink.asFlux()
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
                logger.info("라이더 $riderId의 WebSocket 세션 종료됨: ID=${session.id}, signal=$signal")
                riderSessions.remove(session.id)
                
                // 모든 세션이 종료되면 싱크도 제거
                if (riderSessions.isEmpty()) {
                    riderSinksMap.remove(riderId)
                    logger.info("라이더 $riderId의 모든 세션이 종료되어 싱크를 제거합니다.")
                }
            }
    }
    
    /**
     * 경로에서 라이더 ID를 추출합니다.
     * 예: /ws/rider/1 -> 1
     */
    private fun extractRiderId(path: String): Long? {
        val regex = "/ws/rider/(\\d+)".toRegex()
        val matchResult = regex.find(path)
        
        return matchResult?.groupValues?.get(1)?.toLongOrNull()
    }
    
    /**
     * 웰컴 메시지를 전송합니다.
     */
    private fun sendWelcomeMessage(riderId: Long, session: WebSocketSession) {
        val welcomeMessage = mapOf(
            "eventType" to "WELCOME",
            "riderId" to riderId,
            "timestamp" to LocalDateTime.now().toString(),
            "message" to "라이더 웹소켓 서버에 연결되었습니다."
        )
        
        try {
            val json = objectMapper.writeValueAsString(welcomeMessage)
            session.send(Mono.just(session.textMessage(json))).subscribe()
        } catch (e: Exception) {
            logger.error("웰컴 메시지 전송 오류: ${e.message}", e)
        }
    }
    
    /**
     * 특정 라이더에 이벤트를 전송합니다.
     */
    fun sendEventToRider(riderId: Long, event: Map<String, Any>) {
        logger.debug("라이더 $riderId에 이벤트 전송: $event")
        
        val sink = riderSinksMap[riderId]
        if (sink != null) {
            sink.tryEmitNext(event)
            logger.debug("라이더 $riderId에 이벤트 전송 성공")
        } else {
            logger.warn("라이더 $riderId에 연결된 세션이 없습니다.")
        }
    }
    
    /**
     * 여러 라이더에 이벤트를 브로드캐스트합니다.
     */
    fun broadcastEvent(riderIds: List<Long>, event: Map<String, Any>) {
        logger.debug("${riderIds.size}명의 라이더에 이벤트 브로드캐스트: $event")
        
        riderIds.forEach { riderId ->
            sendEventToRider(riderId, event)
        }
    }
    
    /**
     * 모든 라이더에 이벤트를 브로드캐스트합니다.
     */
    fun broadcastToAll(event: Map<String, Any>) {
        logger.debug("모든 라이더에 이벤트 브로드캐스트: $event")
        
        riderSessionsMap.keys.forEach { riderId ->
            sendEventToRider(riderId, event)
        }
    }
    
    /**
     * 연결된 라이더 수를 반환합니다.
     */
    fun getConnectedRidersCount(): Int {
        return riderSessionsMap.size
    }
    
    /**
     * 특정 라이더의 연결된 세션 수를 반환합니다.
     */
    fun getConnectedSessionsCount(riderId: Long): Int {
        return riderSessionsMap[riderId]?.size ?: 0
    }
}
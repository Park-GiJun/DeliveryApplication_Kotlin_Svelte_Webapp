package com.gijun.backend.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.gijun.backend.application.query.order.OrderQueryServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * 매장 알림 웹소켓 핸들러 - 매장 별로 주문 알림 등을 전송합니다.
 */
@Component
class StoreNotificationWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val orderQueryService: OrderQueryServiceImpl
) : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(StoreNotificationWebSocketHandler::class.java)
    
    // 매장 ID를 키로 하는 세션 맵
    private val storeSessionsMap = ConcurrentHashMap<Long, ConcurrentHashMap<String, WebSocketSession>>()
    
    // 매장 ID를 키로 하는 싱크 맵
    private val storeSinksMap = ConcurrentHashMap<Long, Sinks.Many<Map<String, Any>>>()
    
    // 매장 ID를 키로 하는 주문 폴링 구독 맵
    private val storePollingSubscriptions = ConcurrentHashMap<Long, Disposable>()
    
    // 주문 폴링 간격 (초)
    private val POLLING_INTERVAL_SECONDS = 5L
    
    override fun handle(session: WebSocketSession): Mono<Void> {
        // 경로에서 매장 ID 추출 (예: /ws/store/1)
        val path = session.handshakeInfo.uri.path
        val storeId = extractStoreId(path)
        
        if (storeId == null) {
            logger.error("잘못된 웹소켓 경로: $path")
            return session.close()
        }
        
        logger.info("매장 ${storeId}의 WebSocket 세션 연결됨: ID=${session.id}")
        
        // 매장 ID에 해당하는 세션 맵 가져오기
        val storeSessions = storeSessionsMap.computeIfAbsent(storeId) { ConcurrentHashMap() }
        storeSessions[session.id] = session
        
        // 매장 ID에 해당하는 싱크 가져오기
        val storeSink = storeSinksMap.computeIfAbsent(storeId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }
        
        // 웰컴 메시지 전송
        sendWelcomeMessage(storeId, session)
        
        // 주문 폴링 시작 (첫 번째 연결에만)
        if (storeSessions.size == 1) {
            startOrderPolling(storeId)
        }
        
        // 세션에 싱크의 출력 연결
        val output = storeSink.asFlux()
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
                logger.info("매장 ${storeId}의 WebSocket 세션 종료됨: ID=${session.id}, signal=$signal")
                storeSessions.remove(session.id)
                
                // 모든 세션이 종료되면 싱크와 폴링 구독 제거
                if (storeSessions.isEmpty()) {
                    storeSinksMap.remove(storeId)
                    stopOrderPolling(storeId)
                    logger.info("매장 ${storeId}의 모든 세션이 종료되어 싱크와 폴링을 제거합니다.")
                }
            }
    }
    
    /**
     * 경로에서 매장 ID를 추출합니다.
     * 예: /ws/store/1 -> 1
     */
    private fun extractStoreId(path: String): Long? {
        val regex = "/ws/store/(\\d+)".toRegex()
        val matchResult = regex.find(path)
        
        return matchResult?.groupValues?.get(1)?.toLongOrNull()
    }
    
    /**
     * 웰컴 메시지를 전송합니다.
     */
    private fun sendWelcomeMessage(storeId: Long, session: WebSocketSession) {
        val welcomeMessage = mapOf(
            "eventType" to "WELCOME",
            "storeId" to storeId,
            "timestamp" to LocalDateTime.now().toString(),
            "message" to "매장 웹소켓 서버에 연결되었습니다."
        )
        
        try {
            val json = objectMapper.writeValueAsString(welcomeMessage)
            session.send(Mono.just(session.textMessage(json))).subscribe()
        } catch (e: Exception) {
            logger.error("웰컴 메시지 전송 오류: ${e.message}", e)
        }
    }
    
    /**
     * 특정 매장에 이벤트를 전송합니다.
     */
    fun sendEventToStore(storeId: Long, event: Map<String, Any>) {
        logger.debug("매장 ${storeId}에 이벤트 전송: $event")
        
        val sink = storeSinksMap[storeId]
        if (sink != null) {
            sink.tryEmitNext(event)
            logger.debug("매장 ${storeId}에 이벤트 전송 성공")
        } else {
            logger.warn("매장 ${storeId}에 연결된 세션이 없습니다.")
        }
    }
    
    /**
     * 여러 매장에 이벤트를 브로드캐스트합니다.
     */
    fun broadcastEvent(storeIds: List<Long>, event: Map<String, Any>) {
        logger.debug("${storeIds.size}개 매장에 이벤트 브로드캐스트: $event")
        
        storeIds.forEach { storeId ->
            sendEventToStore(storeId, event)
        }
    }
    
    /**
     * 모든 매장에 이벤트를 브로드캐스트합니다.
     */
    fun broadcastToAll(event: Map<String, Any>) {
        logger.debug("모든 매장에 이벤트 브로드캐스트: $event")
        
        storeSessionsMap.keys.forEach { storeId ->
            sendEventToStore(storeId, event)
        }
    }
    
    /**
     * 연결된 매장 수를 반환합니다.
     */
    fun getConnectedStoresCount(): Int {
        return storeSessionsMap.size
    }
    
    /**
     * 특정 매장의 연결된 세션 수를 반환합니다.
     */
    fun getConnectedSessionsCount(storeId: Long): Int {
        return storeSessionsMap[storeId]?.size ?: 0
    }
    
    /**
     * 주문 폴링을 시작합니다.
     */
    private fun startOrderPolling(storeId: Long) {
        if (storePollingSubscriptions.containsKey(storeId)) {
            logger.info("매장 ${storeId}의 주문 폴링이 이미 실행 중입니다.")
            return
        }
        
        logger.info("매장 ${storeId}의 주문 폴링을 시작합니다. 간격: ${POLLING_INTERVAL_SECONDS}초")
        
        // 주문 폴링 구독 시작
        val subscription = orderQueryService.startOrderPolling(storeId, Duration.ofSeconds(POLLING_INTERVAL_SECONDS))
            .subscribe { orders ->
                if (orders.isNotEmpty()) {
                    logger.debug("매장 ${storeId}에 대기 중인 주문 ${orders.size}개 발견")
                    
                    // 주문 목록 이벤트 생성
                    val event = mapOf(
                        "eventType" to "PENDING_ORDERS_UPDATE",
                        "storeId" to storeId,
                        "timestamp" to LocalDateTime.now().toString(),
                        "ordersCount" to orders.size,
                        "orders" to orders
                    )
                    
                    // 해당 매장에 이벤트 전송
                    sendEventToStore(storeId, event)
                }
            }
        
        storePollingSubscriptions[storeId] = subscription
    }
    
    /**
     * 주문 폴링을 중지합니다.
     */
    private fun stopOrderPolling(storeId: Long) {
        storePollingSubscriptions[storeId]?.dispose()
        storePollingSubscriptions.remove(storeId)
        logger.info("매장 ${storeId}의 주문 폴링을 중지했습니다.")
    }
}
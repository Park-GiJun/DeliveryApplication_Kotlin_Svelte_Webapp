package com.gijun.backend.infrastructure.messaging

import com.gijun.backend.domain.model.order.Order
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 주문 이벤트 발행자 - Kafka를 이용한 주문 이벤트 발행
 */
@Component
class OrderEventPublisher(private val kafkaTemplate: KafkaTemplate<String, Any>?) {
    private val logger = LoggerFactory.getLogger(OrderEventPublisher::class.java)
    
    companion object {
        private const val ORDER_CREATED_TOPIC = "order.created"
        private const val ORDER_CANCELLED_TOPIC = "order.cancelled"
        private const val ORDER_STATUS_CHANGED_TOPIC = "order.status-changed"
    }
    
    /**
     * 주문 생성 이벤트 발행
     */
    fun publishOrderCreatedEvent(order: Order) {
        if (kafkaTemplate == null) {
            logger.warn("KafkaTemplate이 null이므로 이벤트를 발행하지 않습니다.")
            return
        }
        
        try {
            logger.info("주문 생성 이벤트 발행: {}", order.orderNumber)
            
            val event = mapOf(
                "eventType" to "ORDER_CREATED",
                "orderId" to order.id,
                "orderNumber" to order.orderNumber,
                "storeId" to order.storeId,
                "customerId" to order.customerId,
                "timestamp" to LocalDateTime.now().toString(),
                "orderData" to order
            )
            
            kafkaTemplate.send(ORDER_CREATED_TOPIC, order.orderNumber, event)
                .whenComplete { result, exception ->
                    if (exception != null) {
                        logger.error("주문 생성 이벤트 발행 실패: {}", exception.message, exception)
                    } else {
                        logger.debug("주문 생성 이벤트 발행 성공: topic={}, partition={}, offset={}", 
                            result.recordMetadata.topic(), 
                            result.recordMetadata.partition(), 
                            result.recordMetadata.offset())
                    }
                }
        } catch (e: Exception) {
            logger.error("주문 생성 이벤트 발행 중 오류 발생: {}", e.message, e)
        }
    }
    
    /**
     * 주문 취소 이벤트 발행
     */
    fun publishOrderCancelledEvent(order: Order, reason: String) {
        if (kafkaTemplate == null) {
            logger.warn("KafkaTemplate이 null이므로 이벤트를 발행하지 않습니다.")
            return
        }
        
        try {
            logger.info("주문 취소 이벤트 발행: {}, 이유: {}", order.orderNumber, reason)
            
            val event = mapOf(
                "eventType" to "ORDER_CANCELLED",
                "orderId" to order.id,
                "orderNumber" to order.orderNumber,
                "storeId" to order.storeId,
                "customerId" to order.customerId,
                "timestamp" to LocalDateTime.now().toString(),
                "cancelReason" to reason,
                "orderData" to order
            )
            
            kafkaTemplate.send(ORDER_CANCELLED_TOPIC, order.orderNumber, event)
                .whenComplete { result, exception ->
                    if (exception != null) {
                        logger.error("주문 취소 이벤트 발행 실패: {}", exception.message, exception)
                    } else {
                        logger.debug("주문 취소 이벤트 발행 성공: topic={}, partition={}, offset={}", 
                            result.recordMetadata.topic(), 
                            result.recordMetadata.partition(), 
                            result.recordMetadata.offset())
                    }
                }
        } catch (e: Exception) {
            logger.error("주문 취소 이벤트 발행 중 오류 발생: {}", e.message, e)
        }
    }
    
    /**
     * 주문 상태 변경 이벤트 발행
     */
    fun publishOrderStatusChangedEvent(order: Order) {
        if (kafkaTemplate == null) {
            logger.warn("KafkaTemplate이 null이므로 이벤트를 발행하지 않습니다.")
            return
        }
        
        try {
            logger.info("주문 상태 변경 이벤트 발행: {}, 상태: {}", order.orderNumber, order.orderStatus)
            
            val event = mapOf(
                "eventType" to "ORDER_STATUS_CHANGED",
                "orderId" to order.id,
                "orderNumber" to order.orderNumber,
                "storeId" to order.storeId,
                "customerId" to order.customerId,
                "timestamp" to LocalDateTime.now().toString(),
                "newStatus" to order.orderStatus.name,
                "orderData" to order
            )
            
            kafkaTemplate.send(ORDER_STATUS_CHANGED_TOPIC, order.orderNumber, event)
                .whenComplete { result, exception ->
                    if (exception != null) {
                        logger.error("주문 상태 변경 이벤트 발행 실패: {}", exception.message, exception)
                    } else {
                        logger.debug("주문 상태 변경 이벤트 발행 성공: topic={}, partition={}, offset={}", 
                            result.recordMetadata.topic(), 
                            result.recordMetadata.partition(), 
                            result.recordMetadata.offset())
                    }
                }
        } catch (e: Exception) {
            logger.error("주문 상태 변경 이벤트 발행 중 오류 발생: {}", e.message, e)
        }
    }
    
    /**
     * 커스텀 주문 이벤트 발행
     */
    fun publishCustomOrderEvent(topic: String, orderNumber: String, eventType: String, payload: Map<String, Any>) {
        if (kafkaTemplate == null) {
            logger.warn("KafkaTemplate이 null이므로 이벤트를 발행하지 않습니다.")
            return
        }
        
        try {
            logger.info("커스텀 주문 이벤트 발행: {}, 유형: {}, 토픽: {}", orderNumber, eventType, topic)
            
            val event = mutableMapOf(
                "eventType" to eventType,
                "orderNumber" to orderNumber,
                "timestamp" to LocalDateTime.now().toString()
            )
            
            // 페이로드를 이벤트에 추가
            event.putAll(payload)
            
            kafkaTemplate.send(topic, orderNumber, event)
                .whenComplete { result, exception ->
                    if (exception != null) {
                        logger.error("커스텀 주문 이벤트 발행 실패: {}", exception.message, exception)
                    } else {
                        logger.debug("커스텀 주문 이벤트 발행 성공: topic={}, partition={}, offset={}", 
                            result.recordMetadata.topic(), 
                            result.recordMetadata.partition(), 
                            result.recordMetadata.offset())
                    }
                }
        } catch (e: Exception) {
            logger.error("커스텀 주문 이벤트 발행 중 오류 발생: {}", e.message, e)
        }
    }
}

package com.gijun.backend.infrastructure.repository.order

import com.gijun.backend.domain.model.order.OrderItem
import com.gijun.backend.domain.repository.order.OrderItemRepository
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 주문 항목 리포지토리 구현 - R2DBC를 이용한 데이터베이스 접근
 */
@Repository
class OrderItemRepositoryImpl(private val template: R2dbcEntityTemplate) : OrderItemRepository {
    private val logger = LoggerFactory.getLogger(OrderItemRepositoryImpl::class.java)
    
    /**
     * 주문 항목 저장
     */
    override fun save(orderItem: OrderItem): Mono<OrderItem> {
        logger.debug("주문 항목 저장: {}", orderItem)
        
        return if (orderItem.id.toInt() == 0) {
            // 새 주문 항목 생성
            template.insert(orderItem)
                .doOnSuccess { savedItem ->
                    logger.debug("새 주문 항목 생성 완료: id={}, orderId={}", savedItem.id, savedItem.orderId)
                }
        } else {
            // 기존 주문 항목 업데이트 - 보통 업데이트할 필요가 없지만 수량 변경 등이 필요할 경우
            template.update(orderItem)
                .doOnSuccess { _ ->
                    logger.debug("주문 항목 업데이트 완료: id={}, orderId={}", orderItem.id, orderItem.orderId)
                }
        }
        .onErrorResume { e ->
            logger.error("주문 항목 저장 중 오류 발생: {}", e.message, e)
            Mono.error(e)
        }
    }
    
    /**
     * ID로 주문 항목 조회
     */
    override fun findById(id: Integer): Mono<OrderItem> {
        logger.debug("ID로 주문 항목 조회: {}", id)
        
        return template.select(OrderItem::class.java)
            .matching(Query.query(Criteria.where("id").`is`(id)))
            .one()
            .doOnSuccess { item ->
                if (item != null) {
                    logger.debug("주문 항목 조회 성공: id={}, orderId={}", item.id, item.orderId)
                } else {
                    logger.debug("주문 항목 조회 결과 없음: id={}", id)
                }
            }
            .onErrorResume { e ->
                logger.error("주문 항목 조회 중 오류 발생: {}", e.message, e)
                Mono.error(e)
            }
    }
    
    /**
     * 주문 ID로 주문 항목 목록 조회
     */
    override fun findByOrderId(orderId: Integer): Flux<OrderItem> {
        logger.debug("주문 ID로 주문 항목 목록 조회: orderId={}", orderId)
        
        return template.select(OrderItem::class.java)
            .matching(Query.query(Criteria.where("orderId").`is`(orderId)))
            .all()
            .doOnComplete {
                logger.debug("주문 ID로 주문 항목 목록 조회 완료: orderId={}", orderId)
            }
            .onErrorResume { e ->
                logger.error("주문 ID로 주문 항목 목록 조회 중 오류 발생: {}", e.message, e)
                Flux.error(e)
            }
    }
    
    /**
     * 메뉴 ID로 주문 항목 목록 조회
     */
    override fun findByMenuId(menuId: Integer): Flux<OrderItem> {
        logger.debug("메뉴 ID로 주문 항목 목록 조회: menuId={}", menuId)
        
        return template.select(OrderItem::class.java)
            .matching(Query.query(Criteria.where("menuId").`is`(menuId)))
            .all()
            .doOnComplete {
                logger.debug("메뉴 ID로 주문 항목 목록 조회 완료: menuId={}", menuId)
            }
            .onErrorResume { e ->
                logger.error("메뉴 ID로 주문 항목 목록 조회 중 오류 발생: {}", e.message, e)
                Flux.error(e)
            }
    }
}

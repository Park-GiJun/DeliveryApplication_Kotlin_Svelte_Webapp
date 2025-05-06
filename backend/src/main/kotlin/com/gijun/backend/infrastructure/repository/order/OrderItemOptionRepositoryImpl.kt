package com.gijun.backend.infrastructure.repository.order

import com.gijun.backend.domain.model.order.OrderItemOption
import com.gijun.backend.domain.repository.order.OrderItemOptionRepository
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 주문 항목 옵션 리포지토리 구현 - R2DBC를 이용한 데이터베이스 접근
 */
@Repository
class OrderItemOptionRepositoryImpl(private val template: R2dbcEntityTemplate) : OrderItemOptionRepository {
    private val logger = LoggerFactory.getLogger(OrderItemOptionRepositoryImpl::class.java)
    
    /**
     * 주문 항목 옵션 저장
     */
    override fun save(orderItemOption: OrderItemOption): Mono<OrderItemOption> {
        logger.debug("주문 항목 옵션 저장: {}", orderItemOption)
        
        return if (orderItemOption.id.toInt() == 0) {
            // 새 주문 항목 옵션 생성
            template.insert(orderItemOption)
                .doOnSuccess { savedOption ->
                    logger.debug("새 주문 항목 옵션 생성 완료: id={}, orderItemId={}", savedOption.id, savedOption.orderItemId)
                }
        } else {
            // 기존 주문 항목 옵션 업데이트 - 보통 업데이트할 필요가 없음
            template.update(orderItemOption)
                .doOnSuccess { _ ->
                    logger.debug("주문 항목 옵션 업데이트 완료: id={}, orderItemId={}", orderItemOption.id, orderItemOption.orderItemId)
                }
        }
        .onErrorResume { e ->
            logger.error("주문 항목 옵션 저장 중 오류 발생: {}", e.message, e)
            Mono.error(e)
        }
    }
    
    /**
     * ID로 주문 항목 옵션 조회
     */
    override fun findById(id: Integer): Mono<OrderItemOption> {
        logger.debug("ID로 주문 항목 옵션 조회: {}", id)
        
        return template.select(OrderItemOption::class.java)
            .matching(Query.query(Criteria.where("id").`is`(id)))
            .one()
            .doOnSuccess { option ->
                if (option != null) {
                    logger.debug("주문 항목 옵션 조회 성공: id={}, orderItemId={}", option.id, option.orderItemId)
                } else {
                    logger.debug("주문 항목 옵션 조회 결과 없음: id={}", id)
                }
            }
            .onErrorResume { e ->
                logger.error("주문 항목 옵션 조회 중 오류 발생: {}", e.message, e)
                Mono.error(e)
            }
    }
    
    /**
     * 주문 ID로 주문 항목 옵션 목록 조회
     */
    override fun findByOrderId(orderId: Integer): Flux<OrderItemOption> {
        logger.debug("주문 ID로 주문 항목 옵션 목록 조회: orderId={}", orderId)
        
        return template.select(OrderItemOption::class.java)
            .matching(Query.query(Criteria.where("orderId").`is`(orderId)))
            .all()
            .doOnComplete {
                logger.debug("주문 ID로 주문 항목 옵션 목록 조회 완료: orderId={}", orderId)
            }
            .onErrorResume { e ->
                logger.error("주문 ID로 주문 항목 옵션 목록 조회 중 오류 발생: {}", e.message, e)
                Flux.error(e)
            }
    }
    
    /**
     * 주문 항목 ID로 주문 항목 옵션 목록 조회
     */
    override fun findByOrderItemId(orderItemId: Integer): Flux<OrderItemOption> {
        logger.debug("주문 항목 ID로 주문 항목 옵션 목록 조회: orderItemId={}", orderItemId)
        
        return template.select(OrderItemOption::class.java)
            .matching(Query.query(Criteria.where("orderItemId").`is`(orderItemId)))
            .all()
            .doOnComplete {
                logger.debug("주문 항목 ID로 주문 항목 옵션 목록 조회 완료: orderItemId={}", orderItemId)
            }
            .onErrorResume { e ->
                logger.error("주문 항목 ID로 주문 항목 옵션 목록 조회 중 오류 발생: {}", e.message, e)
                Flux.error(e)
            }
    }
    
    /**
     * 옵션 항목 ID로 주문 항목 옵션 목록 조회
     */
    override fun findByOptionItemId(optionItemId: Integer): Flux<OrderItemOption> {
        logger.debug("옵션 항목 ID로 주문 항목 옵션 목록 조회: optionItemId={}", optionItemId)
        
        return template.select(OrderItemOption::class.java)
            .matching(Query.query(Criteria.where("optionItemId").`is`(optionItemId)))
            .all()
            .doOnComplete {
                logger.debug("옵션 항목 ID로 주문 항목 옵션 목록 조회 완료: optionItemId={}", optionItemId)
            }
            .onErrorResume { e ->
                logger.error("옵션 항목 ID로 주문 항목 옵션 목록 조회 중 오류 발생: {}", e.message, e)
                Flux.error(e)
            }
    }
}

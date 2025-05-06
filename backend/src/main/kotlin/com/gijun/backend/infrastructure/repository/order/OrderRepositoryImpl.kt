package com.gijun.backend.infrastructure.repository.order

import com.gijun.backend.domain.enums.order.OrderStatus
import com.gijun.backend.domain.model.order.Order
import com.gijun.backend.domain.repository.order.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * 주문 리포지토리 구현 - R2DBC를 이용한 데이터베이스 접근
 */
@Repository
class OrderRepositoryImpl(private val template: R2dbcEntityTemplate) : OrderRepository {
    private val logger = LoggerFactory.getLogger(OrderRepositoryImpl::class.java)
    
    /**
     * 주문 저장
     */
    override fun save(order: Order): Mono<Order> {
        logger.debug("주문 저장: {}", order)
        
        return if (order.id.toInt() == 0) {
            // 새 주문 생성
            template.insert(order)
                .doOnSuccess { savedOrder ->
                    logger.debug("새 주문 생성 완료: id={}, orderNumber={}", savedOrder.id, savedOrder.orderNumber)
                }
        } else {
            // 기존 주문 업데이트
            val id = order.id
            val update = Update.update("orderStatus", order.orderStatus)
                .set("updatedAt", LocalDateTime.now())
            
            // 상태별 시간 업데이트
            when (order.orderStatus) {
                OrderStatus.ACCEPTED -> update.set("acceptedTime", order.acceptedTime)
                OrderStatus.READY -> update.set("readyTime", order.readyTime)
                OrderStatus.DELIVERED -> update.set("deliveredTime", order.deliveredTime)
                OrderStatus.CANCELLED -> update.set("cancelledTime", order.cancelledTime)
                else -> {}
            }
            
            template.update(Order::class.java)
                .matching(Query.query(Criteria.where("id").`is`(id)))
                .apply(update)
                .then(findById(id))
                .doOnSuccess { updatedOrder ->
                    logger.debug("주문 업데이트 완료: id={}, orderNumber={}, status={}", 
                        updatedOrder.id, updatedOrder.orderNumber, updatedOrder.orderStatus)
                }
        }
        .onErrorResume { e ->
            logger.error("주문 저장 중 오류 발생: {}", e.message, e)
            Mono.error(e)
        }
    }
    
    /**
     * ID로 주문 조회
     */
    override fun findById(id: Integer): Mono<Order> {
        logger.debug("ID로 주문 조회: {}", id)
        
        return template.select(Order::class.java)
            .matching(Query.query(Criteria.where("id").`is`(id)))
            .one()
            .doOnSuccess { order ->
                if (order != null) {
                    logger.debug("주문 조회 성공: id={}, orderNumber={}", order.id, order.orderNumber)
                } else {
                    logger.debug("주문 조회 결과 없음: id={}", id)
                }
            }
            .onErrorResume { e ->
                logger.error("주문 조회 중 오류 발생: {}", e.message, e)
                Mono.error(e)
            }
    }
    
    /**
     * 주문 번호로 주문 조회
     */
    override fun findByOrderNumber(orderNumber: String): Mono<Order> {
        logger.debug("주문 번호로 조회: {}", orderNumber)
        
        return template.select(Order::class.java)
            .matching(Query.query(Criteria.where("orderNumber").`is`(orderNumber)))
            .one()
            .doOnSuccess { order ->
                if (order != null) {
                    logger.debug("주문 번호로 조회 성공: id={}, orderNumber={}", order.id, order.orderNumber)
                } else {
                    logger.debug("주문 번호로 조회 결과 없음: orderNumber={}", orderNumber)
                }
            }
            .onErrorResume { e ->
                logger.error("주문 번호로 조회 중 오류 발생: {}", e.message, e)
                Mono.error(e)
            }
    }
    
    /**
     * 고객 ID로 주문 목록 조회 (페이징, 생성일 기준 내림차순)
     */
    override fun findByCustomerIdOrderByCreatedAtDesc(customerId: Int, pageable: Pageable): Flux<Order> {
        logger.debug("고객 ID로 주문 목록 조회: customerId={}, page={}, size={}", 
            customerId, pageable.pageNumber, pageable.pageSize)
        
        return template.select(Order::class.java)
            .matching(
                Query.query(Criteria.where("customerId").`is`(customerId))
                    .sort(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                    .limit(pageable.pageSize)
                    .offset(pageable.offset)
            )
            .all()
            .doOnComplete {
                logger.debug("고객 ID로 주문 목록 조회 완료: customerId={}", customerId)
            }
            .onErrorResume { e ->
                logger.error("고객 ID로 주문 목록 조회 중 오류 발생: {}", e.message, e)
                Flux.error(e)
            }
    }
    
    /**
     * 매장 ID로 주문 목록 조회 (페이징, 생성일 기준 내림차순)
     */
    override fun findByStoreIdOrderByCreatedAtDesc(storeId: Int, pageable: Pageable): Flux<Order> {
        logger.debug("매장 ID로 주문 목록 조회: storeId={}, page={}, size={}", 
            storeId, pageable.pageNumber, pageable.pageSize)
        
        return template.select(Order::class.java)
            .matching(
                Query.query(Criteria.where("storeId").`is`(storeId))
                    .sort(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                    .limit(pageable.pageSize)
                    .offset(pageable.offset)
            )
            .all()
            .doOnComplete {
                logger.debug("매장 ID로 주문 목록 조회 완료: storeId={}", storeId)
            }
            .onErrorResume { e ->
                logger.error("매장 ID로 주문 목록 조회 중 오류 발생: {}", e.message, e)
                Flux.error(e)
            }
    }
    
    /**
     * 주문 상태로 주문 목록 조회
     */
    override fun findByOrderStatus(status: OrderStatus): Flux<Order> {
        logger.debug("주문 상태로 목록 조회: status={}", status)
        
        return template.select(Order::class.java)
            .matching(Query.query(Criteria.where("orderStatus").`is`(status)))
            .all()
            .doOnComplete {
                logger.debug("주문 상태로 목록 조회 완료: status={}", status)
            }
            .onErrorResume { e ->
                logger.error("주문 상태로 목록 조회 중 오류 발생: {}", e.message, e)
                Flux.error(e)
            }
    }
    
    /**
     * 특정 시간 이후 생성된 주문 목록 조회
     */
    override fun findByOrderTimeAfter(time: LocalDateTime): Flux<Order> {
        logger.debug("특정 시간 이후 주문 목록 조회: time={}", time)
        
        return template.select(Order::class.java)
            .matching(Query.query(Criteria.where("orderTime").greaterThan(time)))
            .all()
            .doOnComplete {
                logger.debug("특정 시간 이후 주문 목록 조회 완료: time={}", time)
            }
            .onErrorResume { e ->
                logger.error("특정 시간 이후 주문 목록 조회 중 오류 발생: {}", e.message, e)
                Flux.error(e)
            }
    }
}

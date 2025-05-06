package com.gijun.backend.application.query.order

import com.gijun.backend.domain.model.order.Order
import com.gijun.backend.domain.repository.order.OrderRepository
import com.gijun.backend.domain.repository.order.OrderItemRepository
import com.gijun.backend.domain.repository.order.OrderItemOptionRepository
import com.gijun.backend.domain.repository.customer.CustomerRepository
import com.gijun.backend.domain.repository.store.StoreRepository
import com.gijun.backend.domain.repository.rider.RiderRepository
import com.gijun.backend.domain.repository.delivery.DeliveryRepository
import com.gijun.backend.infrastructure.cache.OrderCacheManager
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 주문 쿼리 서비스 구현 - 주문 정보 조회
 */
@Service
class OrderQueryServiceImpl(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderItemOptionRepository: OrderItemOptionRepository,
    private val customerRepository: CustomerRepository,
    private val storeRepository: StoreRepository,
    private val riderRepository: RiderRepository,
    private val deliveryRepository: DeliveryRepository,
    private val orderCacheManager: OrderCacheManager
) : OrderQueryService {
    private val logger = LoggerFactory.getLogger(OrderQueryServiceImpl::class.java)
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    /**
     * 주문 번호로 주문 상세 정보 조회
     */
    override fun getOrderByNumber(orderNumber: String): Mono<OrderDetailDTO> {
        logger.info("주문 번호로 상세 조회: {}", orderNumber)
        
        // 캐시에서 먼저 조회 시도
        return orderCacheManager.getOrderFromCache(orderNumber)
            .switchIfEmpty(
                // 캐시에 없으면 DB에서 조회
                orderRepository.findByOrderNumber(orderNumber)
                    .doOnSuccess { order ->
                        if (order != null) {
                            // 조회된 주문을 캐시에 저장
                            orderCacheManager.cacheOrder(order)
                        }
                    }
            )
            .flatMap { order ->
                // 주문 상세 정보 구성
                buildOrderDetailDTO(order)
            }
            .doOnError { e ->
                logger.error("주문 상세 조회 중 오류 발생: {}", e.message, e)
            }
    }

    /**
     * 고객 ID로 주문 목록 조회
     */
    override fun getOrdersByCustomerId(customerId: Int, page: Int, size: Int): Flux<OrderSummaryDTO> {
        logger.info("고객 {} 주문 목록 조회 - page: {}, size: {}", customerId, page, size)
        
        val pageable = PageRequest.of(page, size)
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
            .flatMap { order ->
                buildOrderSummaryDTO(order)
            }
            .doOnComplete {
                logger.debug("고객 {} 주문 목록 조회 완료", customerId)
            }
            .doOnError { e ->
                logger.error("고객 주문 목록 조회 중 오류 발생: {}", e.message, e)
            }
    }

    /**
     * 매장 ID로 주문 목록 조회
     */
    override fun getOrdersByStoreId(storeId: Int, page: Int, size: Int): Flux<OrderSummaryDTO> {
        logger.info("매장 {} 주문 목록 조회 - page: {}, size: {}", storeId, page, size)
        
        val pageable = PageRequest.of(page, size)
        return orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable)
            .flatMap { order ->
                buildOrderSummaryDTO(order)
            }
            .doOnComplete {
                logger.debug("매장 {} 주문 목록 조회 완료", storeId)
            }
            .doOnError { e ->
                logger.error("매장 주문 목록 조회 중 오류 발생: {}", e.message, e)
            }
    }

    /**
     * 라이더 ID로 주문 목록 조회
     */
    override fun getOrdersByRiderId(riderId: Int, page: Int, size: Int): Flux<OrderSummaryDTO> {
        logger.info("라이더 {} 주문 목록 조회 - page: {}, size: {}", riderId, page, size)

        // 먼저 배달 정보에서 주문 ID 목록 조회
        val pageable = PageRequest.of(page, size)
        return deliveryRepository.findByRiderIdOrderByCreatedAtDesc(riderId, pageable)
            .flatMap { delivery ->
                // 배달 정보에서 주문 ID를 가져와 주문 정보 조회
                orderRepository.findById(delivery.orderId)
            }
            .flatMap { order ->
                buildOrderSummaryDTO(order)
            }
            .doOnComplete {
                logger.debug("라이더 {} 주문 목록 조회 완료", riderId)
            }
            .doOnError { e ->
                logger.error("라이더 주문 목록 조회 중 오류 발생: {}", e.message, e)
            }
    }

    /**
     * 주문 객체로부터 주문 상세 DTO 구성
     */
    private fun buildOrderDetailDTO(order: Order): Mono<OrderDetailDTO> {
        // 1. 주문 항목 정보 조회
        val orderItemsWithOptions = orderItemRepository.findByOrderId(order.id)
            .flatMap { orderItem ->
                // 각 주문 항목의 옵션 조회
                orderItemOptionRepository.findByOrderItemId(orderItem.id)
                    .collectList()
                    .flatMap { options ->
                        // 메뉴 이름 조회 (실제로는 메뉴 리포지토리에서 조회해야 함)
                        Mono.just(OrderItemDTO(
                            itemId = orderItem.id.toInt(),
                            menuId = orderItem.menuId.toInt(),
                            menuName = "메뉴 이름", // 실제로는 메뉴 리포지토리에서 조회
                            quantity = orderItem.quantity.toInt(),
                            unitPrice = orderItem.unitPrice,
                            totalPrice = orderItem.totalPrice,
                            options = options.map { option ->
                                OrderItemOptionDTO(
                                    optionId = option.id.toInt(),
                                    optionItemId = option.optionItemId.toInt(),
                                    name = option.name,
                                    price = option.price
                                )
                            }
                        ))
                    }
            }
            .collectList()

        // 2. 고객 정보 조회
        val customerInfo = customerRepository.findById(order.customerId)
            .map { customer ->
                Pair(customer.id.toInt(), customer.name)
            }
            .defaultIfEmpty(Pair(order.customerId.toInt(), "알 수 없음"))

        // 3. 매장 정보 조회
        val storeInfo = storeRepository.findById(order.storeId)
            .map { store ->
                Pair(store.id.toInt(), store.name)
            }
            .defaultIfEmpty(Pair(order.storeId.toInt(), "알 수 없음"))

        // 4. 라이더 정보 조회 (배달 정보를 통해)
        val riderInfo = deliveryRepository.findByOrderId(order.id)
            .flatMap { delivery ->
                riderRepository.findById(delivery.riderId)
                    .map { rider ->
                        Triple(rider.id.toInt(), rider.name, calculateEstimatedDeliveryTime(order, delivery))
                    }
            }
            .defaultIfEmpty(Triple(null, null, null))

        // 5. 모든 정보 조합하여 DTO 구성
        return Mono.zip(orderItemsWithOptions, customerInfo, storeInfo, riderInfo)
            .map { tuple ->
                val items = tuple.t1
                val (customerId, customerName) = tuple.t2
                val (storeId, storeName) = tuple.t3
                val (riderId, riderName, estimatedDeliveryTime) = tuple.t4

                OrderDetailDTO(
                    orderId = order.id.toInt(),
                    orderNumber = order.orderNumber,
                    storeId = storeId,
                    storeName = storeName,
                    customerId = customerId,
                    customerName = customerName,
                    status = order.orderStatus.name,
                    orderTime = formatDateTime(order.orderTime),
                    acceptedTime = formatDateTime(order.acceptedTime),
                    readyTime = formatDateTime(order.readyTime),
                    deliveredTime = formatDateTime(order.deliveredTime),
                    cancelledTime = formatDateTime(order.cancelledTime),
                    items = items,
                    totalAmount = order.totalAmount,
                    deliveryFee = order.deliveryFee,
                    payedAmount = order.payedAmount,
                    discountAmount = order.discountAmount,
                    requestStore = order.requestStore,
                    requestRider = order.requestRider,
                    riderId = riderId,
                    riderName = riderName,
                    estimatedDeliveryTime = estimatedDeliveryTime
                )
            }
    }

    /**
     * 주문 객체로부터 주문 요약 DTO 구성
     */
    private fun buildOrderSummaryDTO(order: Order): Mono<OrderSummaryDTO> {
        // 1. 주문 항목 수 조회
        val itemCount = orderItemRepository.findByOrderId(order.id)
            .count()
            .defaultIfEmpty(0L)

        // 2. 고객 정보 조회
        val customerInfo = customerRepository.findById(order.customerId)
            .map { customer ->
                Pair(customer.id.toInt(), customer.name)
            }
            .defaultIfEmpty(Pair(order.customerId.toInt(), "알 수 없음"))

        // 3. 매장 정보 조회
        val storeInfo = storeRepository.findById(order.storeId)
            .map { store ->
                Pair(store.id.toInt(), store.name)
            }
            .defaultIfEmpty(Pair(order.storeId.toInt(), "알 수 없음"))

        // 4. 모든 정보 조합하여 DTO 구성
        return Mono.zip(itemCount, customerInfo, storeInfo)
            .map { tuple ->
                val count = tuple.t1.toInt()
                val (customerId, customerName) = tuple.t2
                val (storeId, storeName) = tuple.t3

                OrderSummaryDTO(
                    orderId = order.id.toInt(),
                    orderNumber = order.orderNumber,
                    storeId = storeId,
                    storeName = storeName,
                    customerId = customerId,
                    customerName = customerName,
                    status = order.orderStatus.name,
                    orderTime = formatDateTime(order.orderTime),
                    totalAmount = order.totalAmount,
                    deliveryFee = order.deliveryFee,
                    itemCount = count
                )
            }
    }

    /**
     * LocalDateTime 포맷팅 유틸리티 메소드
     */
    private fun formatDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(dateTimeFormatter)
    }

    /**
     * 예상 배달 시간 계산
     */
    private fun calculateEstimatedDeliveryTime(order: Order, delivery: com.gijun.backend.domain.model.delivery.Delivery): String? {
        // 픽업되었다면 배달 예상 시간 계산
        if (delivery.pickUpAt != null) {
            // 평균 배달 시간을 15분으로 가정
            val estimatedTime = delivery.pickUpAt.plusMinutes(15)
            return formatDateTime(estimatedTime)
        }
        // 아직 픽업되지 않았다면 null 반환
        return null
    }
}

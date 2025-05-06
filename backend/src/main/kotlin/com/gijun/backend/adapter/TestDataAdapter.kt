package com.gijun.backend.adapter

import com.gijun.backend.application.dto.CustomerDTO
import com.gijun.backend.application.dto.RiderDTO
import com.gijun.backend.application.dto.StoreDTO
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.lang.Long

@Configuration
class TestDataAdapter(private val databaseClient: DatabaseClient) {
    private val logger = LoggerFactory.getLogger(TestDataAdapter::class.java)

    @Bean
    fun testDataRoutes(): RouterFunction<ServerResponse> {
        logger.info("테스트 데이터 API 라우터 초기화")

        return router {
            "/api/test/data".nest {
                GET("/customers") {
                    logger.info("테스트용 고객 목록 조회 요청")

                    val customers = databaseClient.sql("SELECT id, name, phone, email FROM customer WHERE activated_status = 'ACTIVE' LIMIT 10")
                        .map { row ->
                            CustomerDTO(
                                id = row.get("id", java.lang.Long::class.java)?.toLong() ?: 0L,
                                name = row.get("name", String::class.java) ?: "",
                                phone = row.get("phone", String::class.java) ?: "",
                                email = row.get("email", String::class.java) ?: ""
                            )
                        }
                        .all()
                        .collectList()
                        .onErrorResume { e ->
                            logger.error("고객 목록 조회 실패: ${e.message}", e)
                            Mono.just(createDummyCustomers())
                        }

                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(customers, object : ParameterizedTypeReference<List<CustomerDTO>>() {})
                }

                GET("/stores") {
                    logger.info("테스트용 매장 목록 조회 요청")

                    val stores = databaseClient.sql("SELECT id, name, store_category, address, store_status FROM store WHERE activated_status = 'ACTIVE' LIMIT 10")
                        .map { row ->
                            StoreDTO(
                                id = row.get("id", Long::class.java)?.toLong() ?: 0L,
                                name = row.get("name", String::class.java) ?: "",
                                category = row.get("store_category", String::class.java) ?: "",
                                address = row.get("address", String::class.java) ?: "",
                                status = row.get("store_status", String::class.java) ?: ""
                            )
                        }
                        .all()
                        .collectList()
                        .onErrorResume { e ->
                            logger.error("매장 목록 조회 실패: ${e.message}", e)
                            Mono.just(createDummyStores())
                        }

                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(stores, object : ParameterizedTypeReference<List<StoreDTO>>() {})
                }

                GET("/riders") {
                    logger.info("테스트용 라이더 목록 조회 요청")

                    val riders = databaseClient.sql("SELECT id, name, phone, email, rider_status FROM rider WHERE activated_status = 'ACTIVE' LIMIT 10")
                        .map { row ->
                            RiderDTO(
                                id = row.get("id", Long::class.java)?.toLong() ?: 0L,
                                name = row.get("name", String::class.java) ?: "",
                                phone = row.get("phone", String::class.java) ?: "",
                                email = row.get("email", String::class.java) ?: "",
                                status = row.get("rider_status", String::class.java) ?: ""
                            )
                        }
                        .all()
                        .collectList()
                        .onErrorResume { e ->
                            logger.error("라이더 목록 조회 실패: ${e.message}", e)
                            Mono.just(createDummyRiders())
                        }

                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(riders, object : ParameterizedTypeReference<List<RiderDTO>>() {})
                }
            }
        }.also {
            logger.info("테스트 데이터 API 라우팅 설정 완료: /api/test/data")
        }
    }

    private fun createDummyCustomers() = listOf(
        CustomerDTO(1L, "김민준", "010-1111-1111", "kim.minjun@example.com"),
        CustomerDTO(2L, "이서연", "010-2222-2222", "lee.seoyeon@example.com"),
        CustomerDTO(3L, "박지훈", "010-3333-3333", "park.jihoon@example.com"),
        CustomerDTO(4L, "최수아", "010-4444-4444", "choi.sua@example.com"),
        CustomerDTO(5L, "정우진", "010-5555-5555", "jung.woojin@example.com")
    )

    private fun createDummyStores() = listOf(
        StoreDTO(1L, "맛있는 치킨", "KOREAN", "서울시 강남구 역삼동 123-45", "OPEN"),
        StoreDTO(2L, "피자파티", "ITALIAN", "서울시 서초구 서초동 234-56", "OPEN"),
        StoreDTO(3L, "신선한 초밥", "JAPANESE", "서울시 송파구 잠실동 345-67", "OPEN"),
        StoreDTO(4L, "건강 샐러드", "WESTERN", "서울시 마포구 홍대입구 456-78", "OPEN"),
        StoreDTO(5L, "중화명가", "CHINESE", "서울시 중구 명동 567-89", "OPEN")
    )

    private fun createDummyRiders() = listOf(
        RiderDTO(1L, "박배달", "010-1010-1010", "rider1@example.com", "ACTIVE"),
        RiderDTO(2L, "김라이더", "010-2020-2020", "rider2@example.com", "ACTIVE"),
        RiderDTO(3L, "이배송", "010-3030-3030", "rider3@example.com", "ACTIVE"),
        RiderDTO(4L, "최퀵맨", "010-4040-4040", "rider4@example.com", "ACTIVE"),
        RiderDTO(5L, "정배달", "010-5050-5050", "rider5@example.com", "ACTIVE")
    )
}

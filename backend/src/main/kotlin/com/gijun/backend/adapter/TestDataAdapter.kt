package com.gijun.backend.adapter

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

/**
 * 테스트용 데이터 API 라우터 - 고객, 매장, 라이더 목록 조회 기능 제공
 */
@Configuration
class TestDataAdapter(private val databaseClient: DatabaseClient) {
    private val logger = LoggerFactory.getLogger(TestDataAdapter::class.java)

    @Bean
    fun testDataRoutes(): RouterFunction<ServerResponse> {
        logger.info("테스트 데이터 API 라우터 초기화")
        
        return router {
            "/api/test/data".nest {
                GET("/customers") { request ->
                    logger.info("테스트용 고객 목록 조회 요청")
                    
                    // 데이터베이스에서 고객 목록 조회
                    val customers = databaseClient.sql("SELECT id, name, phone, email FROM customer WHERE activated_status = 'ACTIVE' LIMIT 10")
                        .map { row ->
                            mapOf(
                                "id" to row.get("id", Long::class.java),
                                "name" to row.get("name", String::class.java),
                                "phone" to row.get("phone", String::class.java),
                                "email" to row.get("email", String::class.java)
                            )
                        }
                        .all()
                        .collectList()
                        .onErrorResume { e ->
                            logger.error("고객 목록 조회 실패: ${e.message}", e)
                            // 데이터베이스 오류 시 더미 데이터 반환
                            Mono.just(createDummyCustomers())
                        }
                    
                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(customers, List::class.java)
                }
                
                GET("/stores") { request ->
                    logger.info("테스트용 매장 목록 조회 요청")
                    
                    // 데이터베이스에서 매장 목록 조회
                    val stores = databaseClient.sql("SELECT id, name, store_category, address, store_status FROM store WHERE activated_status = 'ACTIVE' LIMIT 10")
                        .map { row ->
                            mapOf(
                                "id" to row.get("id", Long::class.java),
                                "name" to row.get("name", String::class.java),
                                "category" to row.get("store_category", String::class.java),
                                "address" to row.get("address", String::class.java),
                                "status" to row.get("store_status", String::class.java)
                            )
                        }
                        .all()
                        .collectList()
                        .onErrorResume { e ->
                            logger.error("매장 목록 조회 실패: ${e.message}", e)
                            // 데이터베이스 오류 시 더미 데이터 반환
                            Mono.just(createDummyStores())
                        }
                    
                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(stores, List::class.java)
                }
                
                GET("/riders") { request ->
                    logger.info("테스트용 라이더 목록 조회 요청")
                    
                    // 데이터베이스에서 라이더 목록 조회
                    val riders = databaseClient.sql("SELECT id, name, phone, email, rider_status FROM rider WHERE activated_status = 'ACTIVE' LIMIT 10")
                        .map { row ->
                            mapOf(
                                "id" to row.get("id", Long::class.java),
                                "name" to row.get("name", String::class.java),
                                "phone" to row.get("phone", String::class.java),
                                "email" to row.get("email", String::class.java),
                                "status" to row.get("rider_status", String::class.java)
                            )
                        }
                        .all()
                        .collectList()
                        .onErrorResume { e ->
                            logger.error("라이더 목록 조회 실패: ${e.message}", e)
                            // 데이터베이스 오류 시 더미 데이터 반환
                            Mono.just(createDummyRiders())
                        }
                    
                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(riders, List::class.java)
                }
            }
        }.also {
            logger.info("테스트 데이터 API 라우팅 설정 완료: /api/test/data")
        }
    }
    
    private fun createDummyCustomers(): List<Map<String, Any>> {
        return listOf(
            mapOf("id" to 1L, "name" to "김민준", "phone" to "010-1111-1111", "email" to "kim.minjun@example.com"),
            mapOf("id" to 2L, "name" to "이서연", "phone" to "010-2222-2222", "email" to "lee.seoyeon@example.com"),
            mapOf("id" to 3L, "name" to "박지훈", "phone" to "010-3333-3333", "email" to "park.jihoon@example.com"),
            mapOf("id" to 4L, "name" to "최수아", "phone" to "010-4444-4444", "email" to "choi.sua@example.com"),
            mapOf("id" to 5L, "name" to "정우진", "phone" to "010-5555-5555", "email" to "jung.woojin@example.com")
        )
    }
    
    private fun createDummyStores(): List<Map<String, Any>> {
        return listOf(
            mapOf("id" to 1L, "name" to "맛있는 치킨", "category" to "KOREAN", "address" to "서울시 강남구 역삼동 123-45", "status" to "OPEN"),
            mapOf("id" to 2L, "name" to "피자파티", "category" to "ITALIAN", "address" to "서울시 서초구 서초동 234-56", "status" to "OPEN"),
            mapOf("id" to 3L, "name" to "신선한 초밥", "category" to "JAPANESE", "address" to "서울시 송파구 잠실동 345-67", "status" to "OPEN"),
            mapOf("id" to 4L, "name" to "건강 샐러드", "category" to "WESTERN", "address" to "서울시 마포구 홍대입구 456-78", "status" to "OPEN"),
            mapOf("id" to 5L, "name" to "중화명가", "category" to "CHINESE", "address" to "서울시 중구 명동 567-89", "status" to "OPEN")
        )
    }
    
    private fun createDummyRiders(): List<Map<String, Any>> {
        return listOf(
            mapOf("id" to 1L, "name" to "박배달", "phone" to "010-1010-1010", "email" to "rider1@example.com", "status" to "ACTIVE"),
            mapOf("id" to 2L, "name" to "김라이더", "phone" to "010-2020-2020", "email" to "rider2@example.com", "status" to "ACTIVE"),
            mapOf("id" to 3L, "name" to "이배송", "phone" to "010-3030-3030", "email" to "rider3@example.com", "status" to "ACTIVE"),
            mapOf("id" to 4L, "name" to "최퀵맨", "phone" to "010-4040-4040", "email" to "rider4@example.com", "status" to "ACTIVE"),
            mapOf("id" to 5L, "name" to "정배달", "phone" to "010-5050-5050", "email" to "rider5@example.com", "status" to "ACTIVE")
        )
    }
}
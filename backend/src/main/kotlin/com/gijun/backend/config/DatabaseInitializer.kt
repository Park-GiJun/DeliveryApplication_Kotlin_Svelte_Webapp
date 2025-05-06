package com.gijun.backend.config

import io.r2dbc.spi.ConnectionFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
class DatabaseInitializer(
    private val connectionFactory: ConnectionFactory,
    private val databaseClient: DatabaseClient,
    @Value("\${spring.sql.init.mode:never}") private val initMode: String
) {
    private val logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)

    @Bean
    fun initializer(): ConnectionFactoryInitializer {
        logger.info("데이터베이스 초기화 모드: $initMode")

        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)

        if (initMode == "always" || initMode == "embedded") {
            val populator = CompositeDatabasePopulator()

            try {
                populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("schema.sql")))

                populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("data.sql")))

                initializer.setDatabasePopulator(populator)
                logger.info("데이터베이스 초기화 SQL 스크립트가 로드되었습니다.")
            } catch (e: Exception) {
                logger.error("데이터베이스 초기화 SQL 스크립트 로드 중 오류: ${e.message}", e)
            }
        }

        return initializer
    }
}
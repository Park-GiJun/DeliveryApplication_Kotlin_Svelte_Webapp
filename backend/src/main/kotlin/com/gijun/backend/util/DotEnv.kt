package com.gijun.backend.util

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.util.Properties

/**
 * .env 파일에서 환경 변수를 로드하는 유틸리티 클래스
 */
@Component
class DotEnv {
    private val logger = LoggerFactory.getLogger(DotEnv::class.java)
    
    @PostConstruct
    fun init() {
        try {
            val envFile = File(".env")
            if (envFile.exists()) {
                logger.info(".env 파일을 찾았습니다. 환경 변수를 로드합니다.")
                val properties = Properties()
                FileInputStream(envFile).use { fis ->
                    properties.load(fis)
                }
                
                for ((key, value) in properties) {
                    if (System.getenv(key.toString()) == null) {
                        System.setProperty(key.toString(), value.toString())
                        logger.debug("환경 변수 설정: $key")
                    }
                }
                logger.info(".env 파일에서 환경 변수 로드 완료")
            } else {
                logger.warn(".env 파일을 찾을 수 없습니다. 시스템 환경 변수나 기본값을 사용합니다.")
            }
        } catch (e: Exception) {
            logger.error(".env 파일 로드 중 오류 발생: ${e.message}", e)
        }
    }
}

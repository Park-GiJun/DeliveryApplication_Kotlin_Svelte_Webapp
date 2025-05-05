package com.gijun.backend.config

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component

@Configuration
@ConditionalOnProperty(value = ["spring.kafka.enabled"], havingValue = "true", matchIfMissing = true)
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}") 
    private val bootstrapServers: String
) {
    companion object {
        const val TEST_TOPIC = "delivery.test"
    }

    private val logger = LoggerFactory.getLogger(KafkaConfig::class.java)

    @Bean
    fun testTopic(): NewTopic {
        logger.info("Kafka 테스트 토픽 생성: $TEST_TOPIC")
        return TopicBuilder.name(TEST_TOPIC)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        logger.info("Kafka 어드민 설정: $bootstrapServers")
        val configs = mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers
        )
        return KafkaAdmin(configs)
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> {
        logger.info("Kafka 템플릿 생성")
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        val jsonDeserializer = JsonDeserializer<Any>()
        jsonDeserializer.addTrustedPackages("*")

        val configProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "delivery-test-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to jsonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
        return DefaultKafkaConsumerFactory(configProps, StringDeserializer(), jsonDeserializer)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        logger.info("Kafka 리스너 컨테이너 팩토리 생성")
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory()
        return factory
    }
}

@Component
@ConditionalOnProperty(value = ["spring.kafka.enabled"], havingValue = "true", matchIfMissing = true)
class KafkaTestConsumer {
    private val logger = LoggerFactory.getLogger(KafkaTestConsumer::class.java)

    @KafkaListener(topics = [KafkaConfig.TEST_TOPIC], groupId = "delivery-test-group", autoStartup = "false")
    fun listen(message: Map<String, Any>) {
        logger.info("Kafka 메시지 수신: $message")
    }
}

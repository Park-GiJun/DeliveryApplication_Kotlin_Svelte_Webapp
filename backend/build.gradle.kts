import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.gijun"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring WebFlux - 리액티브 웹 서버
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // Redis - 캐싱용 인메모리 데이터베이스
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    
    // Kafka - 이벤트 처리
    implementation("org.springframework.kafka:spring-kafka")


    // OpenAPI/Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")



    // R2DBC + MySQL
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation ("io.r2dbc:r2dbc-spi")
    implementation("dev.miku:r2dbc-mysql:0.8.2.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE") // 또는 더 높은 버전

    // Coroutines - 비동기 작업 처리
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    
    // Jackson JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // Base Dependencies
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Test Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

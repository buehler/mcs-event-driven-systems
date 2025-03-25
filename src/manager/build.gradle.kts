plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ch.unisg.edpo"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java.srcDir("src/main/proto/java")
    }
}

dependencies {
    // Camunda BPM dependencies
    implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest:7.22.0")
    implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:7.22.0")

    // Camunda Spin dependencies
    implementation("org.camunda.bpm:camunda-engine-plugin-spin:7.22.0") // Spin integration with Camunda
    implementation("org.camunda.spin:camunda-spin-dataformat-json-jackson:1.16.0") // JSON data format support for Spin

    // Database support if needed for Camunda
    implementation("com.h2database:h2") // Use another DB if preferred

    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Apache Kafka dependencies
    implementation("org.apache.kafka:kafka-streams")
    implementation("org.springframework.kafka:spring-kafka")

    // Jackson Databind for JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Lombok for reducing boilerplate code
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Protobuf support
    implementation("com.google.protobuf:protobuf-java:4.30.1")

    // Testing dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootBuildImage {
    builder = "paketobuildpacks/builder-jammy-base:latest"
}

tasks.bootJar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

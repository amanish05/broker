plugins {
    id("java")
    id("io.spring.dependency-management") version "1.1.3"
    id("org.springframework.boot") version "3.1.5"
}

group = "org.mandrin.rain"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Database
    runtimeOnly("com.h2database:h2")
    
    // Utility
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-inline:5.2.0") // Mockito inline for mocking final classes (needed for RestTemplate and Java 17+)

    // https://mvnrepository.com/artifact/com.zerodhatech.kiteconnect/kiteconnect
    implementation("com.zerodhatech.kiteconnect:kiteconnect:3.5.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.bootJar {
    archiveFileName.set("broker-service.jar")
}
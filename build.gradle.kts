import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("org.jetbrains.dokka") version "1.7.20"
    id("org.jetbrains.kotlinx.kover") version "0.7.3"

    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    kotlin("plugin.jpa") version "1.9.0"
    kotlin("kapt") version "1.5.30"
    kotlin("plugin.serialization") version "1.9.0"
}

group = "com.restaurant"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://maven.tryformation.com/releases") {
        content {
            includeGroup("com.jillesvangurp")
        }
    }
}

dependencies {
    // Jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Swagger
    implementation("io.springfox:springfox-boot-starter:3.0.0")

    // Query DSL
    api("com.querydsl:querydsl-jpa:")
    kapt(group = "com.querydsl", name = "querydsl-apt", classifier = "jpa")

    // Mysql
    implementation("mysql:mysql-connector-java")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Jwt
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Logging
    implementation("io.github.microutils:kotlin-logging:1.12.5")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.hibernate.validator:hibernate-validator:6.1.2.Final")

    // AWS SES
    implementation("software.amazon.awssdk:ses:2.20.114")

    // Kotlin
    val coroutineVersion = "1.6.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutineVersion")

    // Es
    implementation("com.jillesvangurp:search-client:2.1.29")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("junit", "junit", "4.13.2")
    testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.springframework.security:spring-security-test")

    // TestContainers
    testImplementation("org.testcontainers:testcontainers:1.17.1")
    testImplementation("org.testcontainers:junit-jupiter:1.17.1")
    testImplementation("org.testcontainers:mysql:1.17.1")
    testImplementation("org.testcontainers:elasticsearch:1.16.2")
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

// Q파일 생성 경로
sourceSets["main"].withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
    kotlin.srcDir("$buildDir/generated/source/kapt/main")
}

tasks {
    withType<Assemble> {
        dependsOn("ktlintFormat")
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    withType<Test> {
        useJUnitPlatform()
        systemProperty("file.encoding", "UTF-8")
    }

    runKtlintCheckOverMainSourceSet {
        dependsOn("kaptKotlin")
    }
}

tasks.jar { enabled = false }

tasks.bootJar { enabled = true }

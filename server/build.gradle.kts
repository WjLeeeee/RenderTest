plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    application
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    // Fat JAR용 플러그인 (배포 필수!)
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.woojin"
version = "1.0.0"

application {
    mainClass.set("com.woojin.server.MainKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // Ktor 서버 코어
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    implementation("ch.qos.logback:logback-classic:1.4.7")

    // DB 관련 (H2, Exposed)
    implementation("com.h2database:h2:2.2.224")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")

    // 코루틴 (서버용)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    id("net.researchgate.release") version "3.0.2"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-thymeleaf")
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("javax.xml.bind:jaxb-api:2.3.1")
    api("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.telegram:telegrambots:6.5.0")
    runtimeOnly("com.mysql:mysql-connector-j:8.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

group = "org.smarthata"
description = "smarthata-server"


tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
}

release.git.requireBranch = "master"

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

tasks.test {
    maxParallelForks = 2
    useJUnitPlatform()
}

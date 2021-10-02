val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.5.30"
}

group = "ktor"
version = "0.0.1"
application {
    mainClass.set("ktor.ApplicationKt")
}

repositories {
    mavenCentral()
    maven { url = uri("https://wansuko-cmd.github.io/maven/") }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")

    //Test
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")

    //Content-Type-Checker
    implementation("com.wsr:content-type-checker:0.0.3")
}

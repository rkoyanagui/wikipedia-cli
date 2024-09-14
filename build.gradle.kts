plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.kotlin.kapt") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.2"
}

version = "1.0.0"
group = "com.rkoyanagui.wikipediacli"

val kotlinVersion = project.properties["kotlinVersion"]
repositories {
    mavenCentral()
}

dependencies {
    kapt("info.picocli:picocli-codegen")
    kapt("io.micronaut.serde:micronaut-serde-processor")
    implementation("info.picocli:picocli")
    implementation("io.micrometer:context-propagation")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.picocli:micronaut-picocli")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("org.yaml:snakeyaml")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.wiremock:wiremock:3.9.1")
    testImplementation("io.micronaut.test:micronaut-test-kotest5")
    testImplementation("io.kotest:kotest-framework-datatest")
    testImplementation("io.mockk:mockk")
    testImplementation("io.kotest:kotest-runner-junit5-jvm")
    kaptTest("io.micronaut:micronaut-inject-java")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
}

application {
    mainClass = "com.rkoyanagui.wikipediacli.WikipediaCliCommand"
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

micronaut {
    testRuntime("kotest5")
    processing {
        incremental(true)
        annotations("com.rkoyanagui.wikipediacli.*")
    }
}

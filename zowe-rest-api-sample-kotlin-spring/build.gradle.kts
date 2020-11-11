import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

val zoweArtifactoryRepository: String by project
val zoweArtifactoryUser: String by project
val zoweArtifactoryPassword: String by project
val newZoweArtifactoryRepository: String by project

plugins {
    jacoco
    id("org.springframework.boot") version "2.3.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.github.hierynomus.license") version "0.15.0"
    id("org.unbroken-dome.test-sets") version "3.0.1"
    id("com.adarshr.test-logger") version "2.1.0"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
}

group = "org.zowe.sample.kotlin"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    maven {
        setUrl(zoweArtifactoryRepository)
    }
}

testSets {
    "integrationTest" { createArtifact = true }
}

testlogger {
    theme = ThemeType.MOCHA
}

license {
    header = rootProject.file(".licence/Apache-or-EPL-License-Header.txt")
    excludes(listOf("**/*.yml", "**/*.json", "**/*.sh", "**/*.txt", "**/*.p12", "**/*.xml", "**/*.jsp", "**/*.html", "**/*.jks", "**/*.so", "**/*.md"))
    mapping("kt", "SLASHSTAR_STYLE")
}

dependencies {
    implementation("org.zowe:zowe-rest-api-commons-spring:1.0.0") {
        exclude(group = "com.ca.mfaas.sdk", module = "mfaas-integration-enabler-java")
    }
    implementation("org.zowe.apiml.sdk:onboarding-enabler-spring-v2-springboot-2.1.1.RELEASE:1.13.0") {
        exclude(group = "joda-time")
    }

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springdoc:springdoc-openapi-ui:1.4.3")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.4.3")

    implementation("io.github.microutils:kotlin-logging:1.8.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    // RestAssured 4.3 is not compatible with AdoptOpenJSK OpenJ9
    // https://github.com/rest-assured/rest-assured/issues/1295
    testImplementation("io.rest-assured:rest-assured:4.2.0")
    testImplementation("io.rest-assured:spring-mock-mvc:4.2.0")
    testImplementation("io.rest-assured:json-path:4.2.0")
    testImplementation("io.rest-assured:xml-path:4.2.0")
    testImplementation("io.rest-assured:spring-mock-mvc-kotlin-extensions:4.2.0")
    testImplementation("io.rest-assured:kotlin-extensions:4.2.0")
    "integrationTestImplementation"("org.awaitility:awaitility-kotlin:4.0.3")
}

tasks.getByName<BootRun>("bootRun") {
    args("--spring.config.additional-location=file:./config/local/local.yml")
}

tasks.getByName<Test>("integrationTest") {
    outputs.upToDateWhen { false }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
    dependsOn("test")
    doLast {
        println("JaCoCo Test report written to: ${jacoco.reportsDir.absoluteFile}/test/html/index.html")
    }
}

tasks.check {
    dependsOn("jacocoTestReport")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

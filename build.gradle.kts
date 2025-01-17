import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "xyz.tzpro.core"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.5.11"
val junitJupiterVersion = "5.11.4"

val mainVerticleName = "Zephyr.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-health-check")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-auth-otp")
  implementation("io.vertx:vertx-web-openapi")
  implementation("io.vertx:vertx-circuit-breaker")
  implementation("io.vertx:vertx-json-schema")
  implementation("io.vertx:vertx-web-openapi-router")
  implementation("io.vertx:vertx-web-api-contract")
  implementation("io.vertx:vertx-tcp-eventbus-bridge")
  implementation("io.vertx:vertx-mysql-client:4.5.11")
  implementation("io.vertx:vertx-web-client:$vertxVersion")
  implementation("org.flywaydb:flyway-mysql:8.2.1")
  implementation("io.github.cdimascio:dotenv-java:3.1.0")
  implementation("io.netty:netty-all:4.1.117.Final")
  implementation("org.hibernate:hibernate-core:6.6.4.Final")
  implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
  implementation("org.hibernate:hibernate-entitymanager:5.6.15.Final")
  implementation("com.zaxxer:HikariCP:5.0.1")
  implementation("mysql:mysql-connector-java:8.0.27")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}
tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}

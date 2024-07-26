plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.15.4"
  kotlin("plugin.spring") version "1.9.23"
  kotlin("plugin.jpa") version "1.9.23"
}

val camelVersion = "4.6.0"
val awssdkVersion = "1.12.756"

dependencies {

  implementation("aws.sdk.kotlin:s3:1.2.38")
  //implementation("// https://mvnrepository.com/artifact/software.amazon.awssdk/s3
  // implementation("software.amazon.awssdk:s3:2.26.15")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:0.2.4")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:3.1.3")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  runtimeOnly("com.h2database:h2:2.1.214")
  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:42.7.2")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("org.springframework.boot:spring-boot-starter-cache")

  implementation("org.apache.camel.springboot:camel-spring-boot-starter:4.3.0")
  implementation("org.apache.camel.springboot:camel-spring-boot-bom:4.3.0")
  implementation("org.apache.camel.springboot:camel-core-starter:4.3.0")
  implementation("org.apache.camel.springboot:camel-bean-starter:4.3.0")
  implementation("org.apache.camel.springboot:camel-log-starter:4.3.0")
  implementation("org.apache.camel.springboot:camel-bean-starter:4.3.0")
  implementation("org.apache.camel.springboot:camel-timer-starter:4.3.0")
  implementation("org.apache.camel.springboot:camel-csv-starter:4.3.0")
  implementation("org.apache.camel.springboot:camel-xslt-starter:4.3.0")

  implementation("org.apache.commons:commons-pool2:2.12.0")

  implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4")
/*
  implementation("org.apache.camel:camel-management:4.6.0")
 // implementation("org.apache.camel.springboot:camel-spring-boot:$camelVersion")
  implementation("org.apache.camel:camel-core:$camelVersion")
  implementation("org.apache.camel:camel-bean:$camelVersion")
  implementation("org.apache.camel:camel-csv:$camelVersion")

  implementation("org.apache.camel:camel-xml-jaxp:$camelVersion")
  implementation("org.apache.camel:camel-timer:$camelVersion")
*/
  implementation("net.javacrumbs.shedlock:shedlock-spring:5.2.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.42.0")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

  implementation("io.jsonwebtoken:jjwt:0.12.3")

  implementation("org.apache.commons:commons-lang3:3.14.0")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  // implementation("org.apache.camel.springboot:camel-spring-boot-starter:$camelVersion")
  implementation("ch.qos.logback:logback-core:1.5.6")

  testImplementation("junit:junit:4.13.2")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")

  testImplementation("org.testcontainers:postgresql:1.19.7")

  testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
  testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")

  testImplementation("org.wiremock:wiremock-standalone:3.5.3")
  testImplementation("com.google.code.gson:gson:2.10.1")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.1")

  testImplementation("org.apache.camel:camel-test-spring-junit5:4.6.0")
  testImplementation("org.apache.camel.springboot:camel-spring-boot-starter:4.6.0")

}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
  withType<Test> {
    exclude("**")

  }
  withType<Task> {
    ktlintCheck {
      enabled = false
    }
  }
  withType<Task> {
    ktlintCheck {
      enabled = false
    }
  }
  withType<Task> {
    ktlintTestSourceSetCheck {
      enabled = false
    }
  }
  withType<Task> {
    ktlintMainSourceSetCheck {
      enabled = false
    }
  }
  withType<Task> {
    ktlintKotlinScriptCheck {
      enabled = false
    }
  }
}

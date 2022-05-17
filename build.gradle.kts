plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.1.6"
  kotlin("plugin.spring") version "1.6.21"
  kotlin("plugin.jpa") version "1.6.21"
}

configurations {
  implementation { exclude(group = "tomcat-jdbc") }
  implementation { exclude(module = "spring-boot-graceful-shutdown") }
}

dependencyCheck {
  suppressionFiles.add("suppressions.xml")
}

val camelVersion = "3.16.0"

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  runtimeOnly("com.h2database:h2:2.1.212")
  runtimeOnly("org.flywaydb:flyway-core:8.5.11")
  runtimeOnly("org.postgresql:postgresql:42.3.5")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

  // NOTE spring-boot-devtools does not currently play nicely with spring-data-redis,
  // see https://github.com/spring-projects/spring-boot/issues/11822, which claims to be fixed but is not.
  implementation("org.springframework.data:spring-data-redis")
  // Note spring-data-redis 2.6.2 does not support Jedis 4.x
  implementation("redis.clients:jedis:3.8.0")

  implementation("org.springframework.cloud:spring-cloud-starter-aws-messaging:2.2.6.RELEASE")
  implementation("org.springframework:spring-jms:5.3.19")
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.0.8")

  implementation("org.apache.camel.springboot:camel-spring-boot:$camelVersion")
  implementation("org.apache.camel:camel-bean:$camelVersion")
  implementation("org.apache.camel:camel-csv:$camelVersion")
  implementation("org.apache.camel:camel-aws2-s3:$camelVersion")
  implementation("org.apache.camel:camel-aws2-sqs:$camelVersion")
  implementation("org.apache.camel:camel-xml-jaxp:$camelVersion")
  implementation("org.apache.camel:camel-timer:$camelVersion")

  implementation("net.javacrumbs.shedlock:shedlock-spring:4.34.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.34.0")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.8")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.8")
  implementation("org.springdoc:springdoc-openapi-security:1.6.6")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")

  implementation("io.jsonwebtoken:jjwt:0.9.1")

  implementation("net.sf.ehcache:ehcache:2.10.9.2")
  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("org.apache.commons:commons-text:1.9")
  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  testImplementation("junit:junit:4.13.2")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("org.apache.camel:camel-test-spring:$camelVersion")
  testImplementation("org.testcontainers:localstack:1.17.1")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("com.google.code.gson:gson:2.9.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
}

tasks {
  compileKotlin {
    // See machine executor in config.yml
    // kotlinOptions.jvmTarget = "16"
  }
}

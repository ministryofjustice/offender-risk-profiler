plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.4"
  kotlin("plugin.spring") version "1.5.21"
  kotlin("plugin.jpa") version "1.5.21"
  idea
}

allOpen {
  annotations(
    "javax.persistence.Entity",
    "javax.persistence.MappedSuperclass",
    "javax.persistence.Embeddable"
  )
}

configurations {
  implementation { exclude(group = "tomcat-jdbc") }
  implementation { exclude(module = "spring-boot-graceful-shutdown") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:42.2.22")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

  // NOTE spring-boot-devtools does not currently play nicely with spring-data-redis,
  // see https://github.com/spring-projects/spring-boot/issues/11822, which claims to be fixed but is not.
  implementation("org.springframework.data:spring-data-redis")
  implementation("redis.clients:jedis:3.6.2")

  implementation("org.springframework.cloud:spring-cloud-starter-aws-messaging:2.2.6.RELEASE")
  implementation("org.springframework:spring-jms:5.3.9")
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.0.8")

  implementation("org.apache.camel.springboot:camel-spring-boot:3.11.0")
  implementation("org.apache.camel:camel-bean:3.11.0")
  implementation("org.apache.camel:camel-csv:3.11.0")
  implementation("org.apache.camel:camel-aws2-s3:3.11.0")
  implementation("org.apache.camel:camel-aws2-sqs:3.11.0")
  implementation("org.apache.camel:camel-xml-jaxp:3.11.0")
  implementation("org.apache.camel:camel-timer:3.11.0")

  implementation("net.javacrumbs.shedlock:shedlock-spring:4.25.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.25.0")

  implementation("javax.annotation:javax.annotation-api:1.3.2")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("com.sun.xml.bind:jaxb-impl:2.3.3")
  implementation("com.sun.xml.bind:jaxb-core:2.3.0.1")
  implementation("javax.activation:activation:1.1.1")
  implementation("javax.transaction:javax.transaction-api:1.3")

  implementation("io.springfox:springfox-swagger2:2.9.2")
  implementation("io.springfox:springfox-swagger-ui:2.9.2")

  implementation("io.jsonwebtoken:jjwt:0.9.1")

  implementation("net.sf.ehcache:ehcache:2.10.9.2")
  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("org.apache.commons:commons-text:1.9")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.4")
  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  testImplementation("junit:junit:4.13.2")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("org.apache.camel:camel-test-spring:3.11.0")
  testImplementation("org.testcontainers:localstack:1.15.3")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("com.google.code.gson:gson:2.8.7")
  testImplementation("com.nhaarman:mockito-kotlin-kt1.1:1.6.0")
  testImplementation("org.awaitility:awaitility-kotlin:4.1.0")
}

tasks {
  compileKotlin {
    // kotlinOptions.jvmTarget = "16"
  }
}

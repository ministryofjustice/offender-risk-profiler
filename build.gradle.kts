plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.8.4"
  kotlin("plugin.spring") version "1.8.10"
  kotlin("plugin.jpa") version "1.8.10"
}

configurations {
  implementation { exclude(group = "tomcat-jdbc") }
  implementation { exclude(module = "spring-boot-graceful-shutdown") }
  all {
    exclude(group = "software.amazon.ion", module = "ion-java")
    exclude(group = "ch.qos.logback", module = "logback-core")
    exclude(group = "ch.qos.logback", module = "logback-classic")
  }
}

dependencyCheck {
  suppressionFiles.add("suppressions.xml")
}

val awssdkVersion = "1.12.468"

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.slf4j:slf4j-simple:2.0.16")
  implementation("org.slf4j:slf4j-api:2.0.16")

  runtimeOnly("com.h2database:h2:2.1.214")
  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:42.7.2")

  implementation("org.springframework.cloud:spring-cloud-starter-aws-messaging:2.2.6.RELEASE")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("io.projectreactor.netty:reactor-netty-core:1.0.39")
  implementation("io.projectreactor.netty:reactor-netty-http:1.0.39")
  implementation("io.netty:netty-codec-http2:4.1.112.Final")
  implementation("org.apache.tomcat.embed:tomcat-embed-core:9.0.93")

  implementation("org.springframework:spring-expression:5.3.27")

  implementation("org.springframework.boot:spring-boot-starter:2.7.9")

  implementation("org.springframework.security:spring-security-config:5.7.10")
  implementation("org.springframework.boot:spring-boot-actuator-autoconfigure:2.7.11")
  implementation("org.springframework.boot:spring-boot-autoconfigure:2.7.12")
  implementation("org.springframework.security:spring-security-web:5.7.12")
  implementation("org.springframework.security:spring-security-core:5.7.12")
  implementation("org.springframework:spring-aop:5.3.34")

  implementation("org.springframework:spring-web:5.3.39")

  // NOTE spring-boot-devtools does not currently play nicely with spring-data-redis,
  // see https://github.com/spring-projects/spring-boot/issues/11822, which claims to be fixed but is not.
  implementation("org.springframework.data:spring-data-redis")
  // Note spring-data-redis 2.6.2 does not support Jedis 4.x
  implementation("redis.clients:jedis:3.8.0")

  implementation("org.springframework:spring-jms:5.3.24")
  implementation("com.amazonaws:amazon-sqs-java-messaging-lib:1.1.2")

  implementation("net.javacrumbs.shedlock:shedlock-spring:5.2.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.42.0")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.15")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.15")
  implementation("org.springdoc:springdoc-openapi-security:1.6.15")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

  implementation("io.jsonwebtoken:jjwt:0.9.1")

  implementation("com.opencsv:opencsv:5.9")
  implementation("commons-io:commons-io:2.16.1")

  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  // https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk
  implementation("com.amazonaws:aws-java-sdk-s3:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-cloudformation:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-core:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-ec2:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-kms:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-sns:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-sqs:$awssdkVersion")
  implementation("com.amazonaws:aws-java-sdk-sts:$awssdkVersion")
  implementation("com.amazonaws:jmespath-java:$awssdkVersion")

  implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4")
  implementation("com.microsoft.azure:applicationinsights-logging-logback:2.6.4")

  testImplementation("junit:junit:4.13.2")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
  testImplementation("org.testcontainers:localstack:1.17.6")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("com.google.code.gson:gson:2.10.1")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")

  testImplementation("com.jayway.jsonpath:json-path:2.9.0")
  testImplementation("jakarta.xml.bind:jakarta.xml.bind-api:2.3.3")
  testImplementation("org.assertj:assertj-core:3.22.0")
  testImplementation("org.hamcrest:hamcrest:2.2")
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
  testImplementation("org.mockito:mockito-core:4.5.1")
  testImplementation("org.skyscreamer:jsonassert:1.5.1")
  testImplementation("org.springframework.boot:spring-boot-starter:2.7.9")
  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure:2.7.9")
  testImplementation("org.springframework.boot:spring-boot-test:2.7.9")
  testImplementation("org.xmlunit:xmlunit-core:2.9.1")

  testImplementation("org.springframework.security:spring-security-test")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(18))
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "18"
    }
  }
}

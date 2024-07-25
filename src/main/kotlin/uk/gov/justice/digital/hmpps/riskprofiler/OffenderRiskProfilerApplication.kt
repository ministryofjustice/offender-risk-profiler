package uk.gov.justice.digital.hmpps.riskprofiler

import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories


@SpringBootApplication
// @EnableRedisRepositories(basePackages = ["uk.gov.justice.digital.hmpps.riskprofiler.cache"])
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.riskprofiler.dao"])
@EnableCaching
class OffenderRiskProfilerApplication

fun main(args: Array<String>) {
  runApplication<OffenderRiskProfilerApplication>(*args)
}

@PreDestroy
fun onExit() {
  val log = LoggerFactory.getLogger(OffenderRiskProfilerApplication::class.java)
  log.info("###STOPing###")
  try {
    Thread.sleep((5 * 1000).toLong())
  } catch (e: InterruptedException) {
    log.error("", e)
  }
  log.info("###STOP FROM THE LIFECYCLE###")
}

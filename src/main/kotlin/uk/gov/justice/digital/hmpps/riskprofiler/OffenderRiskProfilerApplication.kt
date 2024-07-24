package uk.gov.justice.digital.hmpps.riskprofiler

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication
@EnableRedisRepositories(basePackages = ["uk.gov.justice.digital.hmpps.riskprofiler.cache"])
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.riskprofiler.dao"])
class OffenderRiskProfilerApplication

fun main(args: Array<String>) {
  runApplication<OffenderRiskProfilerApplication>(*args)
}

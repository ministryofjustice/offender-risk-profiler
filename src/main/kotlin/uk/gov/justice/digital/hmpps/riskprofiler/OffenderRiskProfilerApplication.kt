package uk.gov.justice.digital.hmpps.riskprofiler

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class OffenderRiskProfilerApplication

fun main(args: Array<String>) {
  runApplication<OffenderRiskProfilerApplication>(*args)
}

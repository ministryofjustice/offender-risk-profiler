package uk.gov.justice.digital.hmpps.riskprofiler

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OffenderRiskProfilerApplication

fun main(args: Array<String>) {
  runApplication<OffenderRiskProfilerApplication>(*args)
}

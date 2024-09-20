package uk.gov.justice.digital.hmpps.riskprofiler

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class OffenderRiskProfilerApplication {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      SpringApplication.run(OffenderRiskProfilerApplication::class.java, *args)
    }
  }
}

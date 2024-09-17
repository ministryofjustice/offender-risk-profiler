package uk.gov.justice.digital.hmpps.riskprofiler.schedule

import com.microsoft.applicationinsights.TelemetryClient
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.services.NomisService
import uk.gov.justice.digital.hmpps.riskprofiler.services.PollPrisonersService
import uk.gov.justice.digital.hmpps.riskprofiler.services.PrisonService

@Service
class PollPrisonersScheduler(
  private val nomisService: NomisService,
  private val pollPrisonersService: PollPrisonersService,
  private val prisonService: PrisonService,
  private val telemetryClient: TelemetryClient?,
) {
  @Scheduled(cron = "0 15 2 * * MON-SAT")
  @SchedulerLock(name = "pollPrisonersLock")
  fun pollPrisonersSchedule() {
    try {
      log.info("Starting: Poll of all prisoners")
      pollPrisoners()
      log.info("Complete: Poll of all prisoners")
    } catch (e: Exception) {
      log.error("pollPrisoners: Global exception handler", e)
      telemetryClient?.trackException(e)
    }
  }

  fun pollPrisoners() {
    val prisons = prisonService.prisons
    log.info("There are {} prisons", prisons.size)
    prisons.forEach { (prisonId) ->
      val prisoners = nomisService.getOffendersAtPrison(prisonId)
      log.info("pollPrisoners: {} contains {} prisoners", prisonId, prisoners.size)
      prisoners.forEach { pollPrisonersService.pollPrisoner(it) }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(PollPrisonersScheduler::class.java)
  }
}

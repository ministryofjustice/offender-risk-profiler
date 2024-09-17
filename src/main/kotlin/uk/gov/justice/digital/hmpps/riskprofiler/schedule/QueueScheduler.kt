package uk.gov.justice.digital.hmpps.riskprofiler.schedule

import com.microsoft.applicationinsights.TelemetryClient
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.events.QueueAdminService

@Service
class QueueScheduler(
  private val queueAdminService: QueueAdminService,
  private val telemetryClient: TelemetryClient?,
) {
  @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
  @SchedulerLock(name = "eventLock")
  fun eventSchedule() {
    try {
      log.info("Starting: Event DLQ")
      queueAdminService.transferEventMessages()
      log.info("Complete: Event DLQ")
    } catch (e: Exception) {
      log.error("eventSchedule: Global exception handler", e)
      telemetryClient?.trackException(e)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(QueueScheduler::class.java)
  }
}

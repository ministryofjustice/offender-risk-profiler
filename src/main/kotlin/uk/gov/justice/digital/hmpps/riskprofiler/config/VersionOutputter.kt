package uk.gov.justice.digital.hmpps.riskprofiler.config

import com.microsoft.applicationinsights.extensibility.ContextInitializer
import com.microsoft.applicationinsights.telemetry.TelemetryContext
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
class VersionOutputter(private val buildProperties: BuildProperties?) {
  @EventListener(ApplicationReadyEvent::class)
  fun logVersionOnStartup() {
    log.info("Version {} started", if (buildProperties == null) "NONE" else buildProperties.version)
  }

  @Bean
  fun versionContextInitializer(): ContextInitializer {
    return ContextInitializer { telemetryContext: TelemetryContext -> telemetryContext.component.setVersion(if (buildProperties == null) "NONE" else buildProperties.version) }
  }

  companion object {
    private val log = LoggerFactory.getLogger(VersionOutputter::class.java)
  }
}

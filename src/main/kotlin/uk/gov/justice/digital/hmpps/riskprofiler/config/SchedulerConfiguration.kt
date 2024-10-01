package uk.gov.justice.digital.hmpps.riskprofiler.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Configuration
class SchedulerConfiguration : SchedulingConfigurer {
  @Bean
  fun taskExecutor(): Executor {
    return Executors.newScheduledThreadPool(12)
  }

  override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
    taskRegistrar.setScheduler(taskExecutor())
  }
}

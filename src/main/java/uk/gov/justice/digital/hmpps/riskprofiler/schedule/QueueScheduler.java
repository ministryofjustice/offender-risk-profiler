package uk.gov.justice.digital.hmpps.riskprofiler.schedule;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.events.QueueAdminService;

@Service
@Slf4j
public class QueueScheduler {

    private final QueueAdminService queueAdminService;
    private final TelemetryClient telemetryClient;

    public QueueScheduler(
            final QueueAdminService queueAdminService,
            final TelemetryClient telemetryClient) {
        this.queueAdminService = queueAdminService;
        this.telemetryClient = telemetryClient;
    }

    @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
    @SchedulerLock(name = "eventLock")
    public void eventSchedule() {
        try {
            log.info("Starting: Event DLQ");
            queueAdminService.transferEventMessages();
            log.info("Complete: Event DLQ");
        } catch (final Exception e) {
            log.error("eventSchedule: Global exception handler", e);
            telemetryClient.trackException(e);
        }
    }
}

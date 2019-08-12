package uk.gov.justice.digital.hmpps.riskprofiler.schedule;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.services.NomisService;
import uk.gov.justice.digital.hmpps.riskprofiler.services.PollPrisonersService;
import uk.gov.justice.digital.hmpps.riskprofiler.services.PrisonService;

@Service
@Slf4j
public class PollPrisonersScheduler {

    private final NomisService nomisService;
    private final PollPrisonersService pollPrisonersService;
    private final PrisonService prisonService;
    private final TelemetryClient telemetryClient;

    public PollPrisonersScheduler(
            final NomisService nomisService,
            final PollPrisonersService pollPrisonersService,
            final PrisonService prisonService,
            TelemetryClient telemetryClient) {
        this.nomisService = nomisService;
        this.pollPrisonersService = pollPrisonersService;
        this.prisonService = prisonService;
        this.telemetryClient = telemetryClient;
    }

    @Scheduled(cron = "0 15 2 * * *")
    @SchedulerLock(name = "pollPrisonersLock")
    public void pollPrisonersSchedule() {
        try {
            log.info("Starting: Poll of all prisoners");
            pollPrisoners();
            log.info("Complete: Poll of all prisoners");
        } catch (Exception e) {
            log.error("pollPrisoners: Global exception handler", e);
            telemetryClient.trackException(e);
        }
    }

    private void pollPrisoners() {
        final var prisons = prisonService.getPrisons();
        log.info("There are {} prisons", prisons.size());
        prisons.forEach(p -> {
            final var prisoners = nomisService.getOffendersAtPrison(p.getPrisonId());
            log.info("pollPrisoners: {} contains {} prisoners", p.getPrisonId(), prisoners.size());
            prisoners.forEach(pollPrisonersService::pollPrisoner);
        });
    }
}

package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PreviousProfileRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PreviousProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Status;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

@Service
@Slf4j
public class PollPrisonersService {

    private final SocDecisionTreeService socDecisionTreeService;
    private final ViolenceDecisionTreeService violenceDecisionTreeService;
    private final EscapeDecisionTreeService escapeDecisionTreeService;
    private final ExtremismDecisionTreeService extremismDecisionTreeService;
    private final PreviousProfileRepository previousProfileRepository;
    private final TelemetryClient telemetryClient;

    private final ObjectMapper jacksonMapper = new ObjectMapper();

    public PollPrisonersService(
            final SocDecisionTreeService socDecisionTreeService,
            final ViolenceDecisionTreeService violenceDecisionTreeService,
            final EscapeDecisionTreeService escapeDecisionTreeService,
            final ExtremismDecisionTreeService extremismDecisionTreeService,
            final PreviousProfileRepository previousProfileRepository,
            final TelemetryClient telemetryClient) {
        this.socDecisionTreeService = socDecisionTreeService;
        this.violenceDecisionTreeService = violenceDecisionTreeService;
        this.escapeDecisionTreeService = escapeDecisionTreeService;
        this.extremismDecisionTreeService = extremismDecisionTreeService;
        this.previousProfileRepository = previousProfileRepository;
        this.telemetryClient = telemetryClient;
    }

    @Transactional
    public void pollPrisoner(final String offenderNo) {
        try {
            final var socObject = socDecisionTreeService.getSocData(offenderNo);
            final var violenceObject = violenceDecisionTreeService.getViolenceProfile(offenderNo);
            final var escapeObject = escapeDecisionTreeService.getEscapeProfile(offenderNo);
            final var extremismObject = extremismDecisionTreeService.getExtremismProfile(offenderNo, false);

            final var soc = jacksonMapper.writeValueAsString(socObject);
            final var violence = jacksonMapper.writeValueAsString(violenceObject);
            final var escape = jacksonMapper.writeValueAsString(escapeObject);
            final var extremism = jacksonMapper.writeValueAsString(extremismObject);

            // Check if in db
            final Optional<PreviousProfile> previousProfile = previousProfileRepository.findById(offenderNo);
            previousProfile.ifPresentOrElse(
                    existing -> {
                        // Compare with existing stored values
                        if (!(existing.getSoc().equals(soc)
                                && existing.getViolence().equals(violence)
                                && existing.getEscape().equals(escape)
                                && existing.getExtremism().equals(extremism))) {
                            // TODO There is a change. We may only care about when change is from C to B
                            // Possibly add to an event queue.
                            // Update db with new data:
                            log.info("Change detected for {}", offenderNo);

                            existing.setSoc(soc);
                            existing.setViolence(violence);
                            existing.setEscape(escape);
                            existing.setExtremism(extremism);
                            existing.setExecuteDateTime(LocalDateTime.now());
                            existing.setStatus(Status.NEW);
                        }
                    },
                    () -> {
                        // if not there, just add
                        previousProfileRepository.save(PreviousProfile.builder()
                                .offenderNo(offenderNo)
                                .soc(soc)
                                .violence(violence)
                                .escape(escape)
                                .extremism(extremism)
                                .executeDateTime(LocalDateTime.now())
                                .status(Status.NEW)
                                .build());
                        log.info("Added new offender {} to DB", offenderNo);
                    });
        } catch (final Exception e) {
            raiseProcessingError(offenderNo, e);
        }
    }

    private void raiseProcessingError(final String offenderNo, final Exception e) {
        log.error("pollPrisoner: Exception thrown for " + offenderNo, e);
        final var logMap = new HashMap<String, String>();
        logMap.put("offenderNo", offenderNo);
        telemetryClient.trackException(e, logMap, null);
    }
}

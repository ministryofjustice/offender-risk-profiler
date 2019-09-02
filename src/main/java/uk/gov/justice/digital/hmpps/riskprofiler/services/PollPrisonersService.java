package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PreviousProfileRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.*;

import java.io.IOException;
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
    private final SQSService sqsService;
    private final TelemetryClient telemetryClient;

    private final ObjectMapper jacksonMapper = new ObjectMapper();

    public PollPrisonersService(
            final SocDecisionTreeService socDecisionTreeService,
            final ViolenceDecisionTreeService violenceDecisionTreeService,
            final EscapeDecisionTreeService escapeDecisionTreeService,
            final ExtremismDecisionTreeService extremismDecisionTreeService,
            final PreviousProfileRepository previousProfileRepository,
            final TelemetryClient telemetryClient,
            final SQSService sqsService) {
        this.socDecisionTreeService = socDecisionTreeService;
        this.violenceDecisionTreeService = violenceDecisionTreeService;
        this.escapeDecisionTreeService = escapeDecisionTreeService;
        this.extremismDecisionTreeService = extremismDecisionTreeService;
        this.previousProfileRepository = previousProfileRepository;
        this.telemetryClient = telemetryClient;
        this.sqsService = sqsService;
        jacksonMapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
                            // Update db with new data:
                            log.info("Change detected for {}", offenderNo);

                            buildAndSendRiskProfilePayload(offenderNo, socObject, violenceObject, escapeObject, extremismObject, existing);

                            existing.setSoc(soc);
                            existing.setViolence(violence);
                            existing.setEscape(escape);
                            existing.setExtremism(extremism);
                            existing.setExecuteDateTime(LocalDateTime.now());
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
                                .build());
                        log.info("Added new offender {} to DB", offenderNo);
                    });
        } catch (final Exception e) {
            raiseProcessingError(offenderNo, e);
        }
    }

    private void buildAndSendRiskProfilePayload(String offenderNo, SocProfile socObject, ViolenceProfile violenceObject, EscapeProfile escapeObject, ExtremismProfile extremismObject, PreviousProfile existing) {
        final var newProfile = ProfileMessagePayload.builder()
                .soc(socObject)
                .violence(violenceObject)
                .escape(escapeObject)
                .extremism(extremismObject)
                .build();

        final ProfileMessagePayload oldProfile;
        try {
            oldProfile = ProfileMessagePayload.builder()
                    .soc(jacksonMapper.readValue(existing.getSoc(), SocProfile.class))
                    .violence(jacksonMapper.readValue(existing.getViolence(), ViolenceProfile.class))
                    .escape(jacksonMapper.readValue(existing.getEscape(), EscapeProfile.class))
                    .extremism(jacksonMapper.readValue(existing.getExtremism(), ExtremismProfile.class))
                    .build();

            var payload = RiskProfileChange.builder().newProfile(newProfile).oldProfile(oldProfile)
                    .offenderNo(offenderNo).executeDateTime(existing.getExecuteDateTime()).build();
            log.info("Reporting risk change to queue for offender {}", offenderNo);

            sqsService.sendRiskProfileChangeMessage(payload);
        } catch (IOException e) {
            log.error("Problem creating risk profile change message for " +  offenderNo, e);
        }
    }

    private void raiseProcessingError(final String offenderNo, final Exception e) {
        log.error("pollPrisoner: Exception thrown for " + offenderNo, e);
        final var logMap = new HashMap<String, String>();
        logMap.put("offenderNo", offenderNo);
        telemetryClient.trackException(e, logMap, null);
    }
}

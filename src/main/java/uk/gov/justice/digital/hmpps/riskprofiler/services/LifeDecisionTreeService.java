package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.model.LifeProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@Slf4j
public class LifeDecisionTreeService {

    private final NomisService nomisService;

    /**
     * These values come from the population management information IMPRISONMENT_STATUS_SHORT field (JSAS calculated) as used by policy.
     * They actually refer to codes in the IMPRISONMENT_STATUSES Nomis table.
     */
    private static final List<String> LIFE_STATUS = List.of(
            "ALP", "ALP_LASPO", "CFLIFE", "DFL", "DLP", "DIED", "HMPL",
            "LIFE", "MLP", "SEC90_03", "SEC93", "SEC93_03", "SEC94", "SEC19_3B");

    public LifeDecisionTreeService(final NomisService nomisService) {
        this.nomisService = nomisService;
    }

    public LifeProfile getLifeProfile(@NotNull final String nomsId) {
        log.debug("Calculating life profile for {}", nomsId);
        final var bookingId = nomisService.getBooking(nomsId);

        final var lifeFlag = isLifeFlag(bookingId);
        final var lifeStatus = isLifeStatus(bookingId);
        final var murder = isMurder(bookingId);
        final boolean life = lifeFlag || lifeStatus || murder;

        final String cat = life ? "B" : RiskProfile.DEFAULT_CAT;
        log.info("Life result for {}: {} (lifeFlag={} lifeStatus={} murder={})", nomsId, life, lifeFlag, lifeStatus, murder);
        return new LifeProfile(nomsId, cat, life);
    }

    private boolean isLifeFlag(Long bookingId) {
        final var sentenceData = nomisService.getSentencesForOffender(bookingId);
        return sentenceData.stream().anyMatch(s -> Boolean.TRUE.equals(s.getLifeSentence()));
    }

    private boolean isLifeStatus(Long bookingId) {
        final var imprisonmentData = nomisService.getBookingDetails(bookingId);
        return imprisonmentData.stream().anyMatch(s -> s.getImprisonmentStatus() != null && LIFE_STATUS.contains(s.getImprisonmentStatus()));
    }

    private boolean isMurder(Long bookingId) {
        final var mainOffence = nomisService.getMainOffences(bookingId);
        return mainOffence.stream().anyMatch(o -> o.toUpperCase().startsWith("MURDER"));
    }
}

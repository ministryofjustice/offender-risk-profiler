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
        final var sentenceData = nomisService.getSentencesForOffender(bookingId);
        final var imprisonmentData = nomisService.getBookingDetails(bookingId);
        final boolean life = sentenceData.stream().anyMatch(s -> Boolean.TRUE.equals(s.getLifeSentence()))
                || imprisonmentData.stream().anyMatch(s -> s.getImprisonmentStatus() != null && LIFE_STATUS.contains(s.getImprisonmentStatus()));
        final String cat = life ? "B" : RiskProfile.DEFAULT_CAT;
        log.debug("Life result for {}: {}", nomsId, life);
        return new LifeProfile(nomsId, cat, life);
    }
}

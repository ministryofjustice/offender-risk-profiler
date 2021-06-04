package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.ViperRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Viper;
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentResponse;
import uk.gov.justice.digital.hmpps.riskprofiler.model.ViolenceProfile;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile.DEFAULT_CAT;

@Service
@Slf4j
public class ViolenceDecisionTreeService {

    private final BigDecimal viperScoreThreshold;
    private final int minNumAssaults;
    private final int months;

    private final DataRepository<Viper> viperDataRepository;
    private final NomisService nomisService;

    private final static List<SeriousQuestionAndResponse> SERIOUS_ASSAULT_QUESTIONS = List.of(
            SeriousQuestionAndResponse.builder().question("WAS THIS A SEXUAL ASSAULT").needAnswer("YES").build(),
            SeriousQuestionAndResponse.builder().question("WAS MEDICAL TREATMENT FOR CONCUSSION OR INTERNAL INJURIES REQUIRED").needAnswer("YES").build(),
            SeriousQuestionAndResponse.builder().question("WAS A SERIOUS INJURY SUSTAINED").needAnswer("YES").build(),
            SeriousQuestionAndResponse.builder().question("DID INJURIES RESULT IN DETENTION IN OUTSIDE HOSPITAL AS AN IN-PATIENT").needAnswer("YES").build()
    );

    public ViolenceDecisionTreeService(final ViperRepository viperDataRepository, final NomisService nomisService,
                                       @Value("${app.assaults.min:5}") final int minNumAssaults,
                                       @Value("${app.assaults.check.months:12}") final int months,
                                       @Value("${app.viper-threshold:5.00}") final BigDecimal viperScoreThreshold) {
        this.viperDataRepository = viperDataRepository;
        this.nomisService = nomisService;
        this.minNumAssaults = minNumAssaults;
        this.months = months;
        this.viperScoreThreshold = viperScoreThreshold;
    }

    public ViolenceProfile getViolenceProfile(@NotNull final String nomsId) {

        // Check NOMIS Have the individuals had 5 or more assaults in custody? (remove DUPS)
        final var assaults = nomisService.getIncidents(nomsId).stream()
                .filter(i -> !"DUP".equals(i.getIncidentStatus())).collect(Collectors.toList());

        // Check NOMIS: Have they had a serious assault in custody in past 12 months
        final var numberOfSeriousAssaults = assaults.stream()
                .filter(assault -> assault.getReportTime().compareTo(LocalDateTime.now().minusMonths(months)) >= 0)
                .filter(assault -> assault.getResponses().stream()
                        .anyMatch(response -> SERIOUS_ASSAULT_QUESTIONS.stream().anyMatch(saq -> isSerious(response, saq))))
                .count();

        final var numberOfNonSeriousAssaults = assaults.stream()
                .filter(assault -> assault.getReportTime().compareTo(LocalDateTime.now().minusMonths(months)) >= 0)
                .count() - numberOfSeriousAssaults;

        final var violenceProfile = ViolenceProfile.violenceBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(DEFAULT_CAT);

        violenceProfile.displayAssaults(!assaults.isEmpty());
        violenceProfile.numberOfAssaults(assaults.size());
        violenceProfile.numberOfSeriousAssaults(numberOfSeriousAssaults);
        violenceProfile.numberOfNonSeriousAssaults(numberOfNonSeriousAssaults);

        viperDataRepository.getByKey(nomsId).ifPresentOrElse(viper -> {
            if (viper.getScore().compareTo(viperScoreThreshold) > 0) {
                log.debug("violence: Viper score {} above threshold for {}", viper.getScore(), nomsId);
                violenceProfile.notifySafetyCustodyLead(true);

                if (assaults.size() >= minNumAssaults) {
                    log.debug("violence: Viper assaults above threshold for {}", nomsId);

                    if (numberOfSeriousAssaults > 0) {
                        log.debug("violence: Viper serious assaults above threshold for {}", nomsId);
                        violenceProfile.provisionalCategorisation("B");

                    } else {
                        log.debug("violence: Viper serious assaults below threshold for {}", nomsId);
                        violenceProfile.provisionalCategorisation("C");
                    }
                } else {
                    log.debug("violence: Viper assaults below threshold {} for {}", minNumAssaults, nomsId);
                    violenceProfile.provisionalCategorisation("C");
                }
            } else {
                log.debug("violence: Viper score {} is below threshold of {} for {}", viper.getScore(), viperScoreThreshold, nomsId);
                violenceProfile.provisionalCategorisation(DEFAULT_CAT);
            }
        }, () -> {
            log.debug("violence: No Viper score for {}", nomsId);
            violenceProfile.provisionalCategorisation("C");
        }
        );

        return violenceProfile.build();
    }

    private boolean isSerious(final IncidentResponse incidentResponse, final SeriousQuestionAndResponse seriousQuestionAndResponse) {
        return seriousQuestionAndResponse.question.equalsIgnoreCase(incidentResponse.getQuestion())
            && seriousQuestionAndResponse.needAnswer.equalsIgnoreCase(incidentResponse.getAnswer());
    }

    @Builder
    private static class SeriousQuestionAndResponse {
        private final String question;
        private final String needAnswer;
    }
}

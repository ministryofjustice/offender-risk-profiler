package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.ViperRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Viper;
import uk.gov.justice.digital.hmpps.riskprofiler.model.ViolenceProfile;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

import static uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile.DEFAULT_CAT;

@Service
public class ViolenceDecisionTreeService {

    private final DataRepository<Viper> viperDataRepository;
    private final NomisService nomisService;

    public ViolenceDecisionTreeService(ViperRepository viperDataRepository, NomisService nomisService) {
        this.viperDataRepository = viperDataRepository;
        this.nomisService = nomisService;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public ViolenceProfile getViolenceProfile(@NotNull final String nomsId) {

        var violenceProfile = ViolenceProfile.violenceBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(DEFAULT_CAT);


        // TODO: VISOR not for MVP (probably)

        viperDataRepository.getByKey(nomsId).ifPresentOrElse(viper -> {
            if (viper.getScore().compareTo(new BigDecimal("5.00")) > 0) {
                violenceProfile.notifySafetyCustodyLead(true);

                // TODO: Check NOMIS Have the individuals had 5 or more assaults in custody?
               var assaults = nomisService.getAssaults(nomsId);
               if (assaults.size() >= 5) {
                   // TODO: Check NOMIS Have they had a serious assault in custody in past 12 months
                   boolean seriousAssault = assaults.stream()
                           .anyMatch(assault -> assault.isSerious() && assault.getDateCreated().isAfter(LocalDate.now().minusYears(1)));

                   if (seriousAssault) {
                       violenceProfile.provisionalCategorisation("B");
                   } else {
                       violenceProfile.provisionalCategorisation("C");
                   }
               } else {
                   violenceProfile.provisionalCategorisation("C");
               }
            } else {
                violenceProfile.provisionalCategorisation(DEFAULT_CAT);
            }
        }, () -> {
            violenceProfile.provisionalCategorisation("C");
        });

        return violenceProfile.build();

    }
}

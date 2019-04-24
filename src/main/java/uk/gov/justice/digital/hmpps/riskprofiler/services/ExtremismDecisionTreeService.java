package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PathfinderRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;
import uk.gov.justice.digital.hmpps.riskprofiler.model.ExtremismProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Service
@Slf4j
public class ExtremismDecisionTreeService {

    private final DataRepository<PathFinder> repository;

    public ExtremismDecisionTreeService(PathfinderRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public ExtremismProfile getExtremismProfile(@NotNull final String nomsId, Boolean previousOffences) {
        log.debug("Calculating extremism profile for {}", nomsId);
        return decisionProcess(nomsId, Boolean.TRUE.equals(previousOffences), repository.getByKey(nomsId));
    }

    private ExtremismProfile decisionProcess(String nomsId, boolean previousOffences, Optional<PathFinder> pathFinder) {
        var extremism = ExtremismProfile.extremismBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(RiskProfile.DEFAULT_CAT);

        pathFinder.ifPresent(pf -> {
            var banding = StringUtils.upperCase(pf.getPathFinderBanding());
            log.debug("extremism: {} in pathfinder on {}, increased Risk of Extremism", nomsId, banding);
            if (banding.contains("BAND 1") || banding.contains("BAND 2")) {
                extremism.increasedRiskOfExtremism(true);
                extremism.notifyRegionalCTLead(true);
                log.debug("extremism: {} Increased Risk of Extremism", nomsId);

                if (previousOffences) {
                    log.debug("extremism: {} has previous offences", nomsId);
                    extremism.provisionalCategorisation("B");
                } else {
                    extremism.provisionalCategorisation("C");
                }
            } else {
                if (banding.contains("BAND 3")) {
                    log.debug("extremism: {} - just notify Regional CT Lead", nomsId);
                    extremism.notifyRegionalCTLead(true);
                    extremism.provisionalCategorisation("C");
                } else {
                    extremism.provisionalCategorisation("C");
                }
            }
        });

        return extremism.build();
    }
}

package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;
import uk.gov.justice.digital.hmpps.riskprofiler.model.ExtremismProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Service
@Slf4j
public class ExtremismDecisionTreeService {

    private final PathfinderService repository;

    public ExtremismDecisionTreeService(final PathfinderService repository) {
        this.repository = repository;
    }

    public ExtremismProfile getExtremismProfile(@NotNull final String nomsId, final Boolean previousOffences) {
        log.debug("Calculating extremism profile for {}", nomsId);
        final var pathFinder = repository.getBand(nomsId);
        return decisionProcess(nomsId, Boolean.TRUE.equals(previousOffences), pathFinder);
    }

    private ExtremismProfile decisionProcess(final String nomsId, final boolean previousOffences, final Optional<PathFinder> pathFinder) {
        final var extremism = ExtremismProfile.extremismBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(RiskProfile.DEFAULT_CAT);

        pathFinder.ifPresent(pf -> {
            final var banding = pf.getPathFinderBanding();
            log.info("extremism: {} in pathfinder on {}, increased Risk of Extremism", nomsId, banding);
            extremism.increasedRiskOfExtremism(true);
            if (banding == 1 || banding == 2) {
                extremism.notifyRegionalCTLead(true);
                if (previousOffences) {
                    log.info("extremism: {} has previous offences", nomsId);
                    extremism.provisionalCategorisation("B");
                } else {
                    extremism.provisionalCategorisation("C");
                }
            } else {
                if (banding == 3) {
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

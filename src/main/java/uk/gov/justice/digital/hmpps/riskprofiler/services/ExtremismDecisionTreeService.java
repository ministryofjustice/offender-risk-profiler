package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.ExtremismProfile;

import javax.validation.constraints.NotNull;

@Service
public class ExtremismDecisionTreeService {

    private final DataRepository repository;

    public ExtremismDecisionTreeService(DataRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public ExtremismProfile getExtremismProfile(@NotNull final String nomsId, Boolean previousOffences) {
        var pathfinderData = repository.getPathfinderDataByNomsId(nomsId);

        var extremism = ExtremismProfile.extremismBuilder()
                .nomsId(nomsId)
                .build();
        // etc

        return extremism;

    }
}

package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.ViolenceProfile;

import javax.validation.constraints.NotNull;

@Service
public class ViolenceDecisionTreeService {

    private final DataRepository repository;

    public ViolenceDecisionTreeService(DataRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public ViolenceProfile getViolenceProfile(@NotNull final String nomsId) {

        var violenceProfile = ViolenceProfile.violenceBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(RiskProfile.DEFAULT_CAT);

        // etc

        return violenceProfile.build();

    }
}

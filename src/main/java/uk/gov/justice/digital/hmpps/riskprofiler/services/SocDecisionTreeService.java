package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrasRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile;

import javax.validation.constraints.NotNull;

@Service
public class SocDecisionTreeService {

    private final PrasRepository repository;

    public SocDecisionTreeService(PrasRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public SocProfile getSocData(@NotNull final String nomsId) {
        var prasData = repository.getPrasDataByNomsId(nomsId);

        var soc = SocProfile.socBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(RiskProfile.DEFAULT_CAT);

        if (prasData.isPresent()) {
            soc.transferToSecurity(true);
        }

        // etc

        return soc.build();

    }
}

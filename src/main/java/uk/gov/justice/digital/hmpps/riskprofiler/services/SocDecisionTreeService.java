package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile;

import javax.validation.constraints.NotNull;

@Service
public class SocDecisionTreeService {

    private final DataRepository repository;

    public SocDecisionTreeService(DataRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public SocProfile getSocData(@NotNull final String nomsId) {
        var prasData = repository.getPrasDataByNomsId(nomsId);

        var soc = SocProfile.socBuilder()
                .nomsId(nomsId)
                .build();

        if (prasData.isPresent()) {
            soc.setTransferToSecurity(true);
            soc.setProvisionalCategorisation("C");
        }

        // etc

        return soc;

    }
}

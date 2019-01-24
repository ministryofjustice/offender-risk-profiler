package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeProfile;

import javax.validation.constraints.NotNull;

@Service
public class EscapeDecisionTreeService {

    private final NomisService nomisService;

    public EscapeDecisionTreeService(NomisService nomisService) {
        this.nomisService = nomisService;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public EscapeProfile getEscapeProfile(@NotNull final String nomsId) {
        var escapeData = nomisService.getEscapeList(nomsId);

        var escape = EscapeProfile.escapeBuilder()
                .nomsId(nomsId)
                .build();

        // etc

        return escape;

    }
}

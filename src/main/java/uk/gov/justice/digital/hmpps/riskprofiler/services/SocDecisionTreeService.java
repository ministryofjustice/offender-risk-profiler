package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
public class SocDecisionTreeService {

    private final DataRepository repository;

    public SocDecisionTreeService(DataRepository repository) {
        this.repository = repository;
    }

    public SocProfile getSocData(@NotNull final String nomsId) {
        var prasData = repository.getPrasData();

        var soc = SocProfile.socBuilder()
                .nomsId(nomsId)
                .build();

        Optional<List<String>> matchingNomsIdLine = prasData.stream()
                .filter(pras -> nomsId.equalsIgnoreCase(pras.get(11))).findFirst();

        if (matchingNomsIdLine.isPresent()) {
            soc.setTransferToSecurity(true);
            soc.setProvisionalCategorisation("C");
        }

        // etc

        return soc;

    }
}

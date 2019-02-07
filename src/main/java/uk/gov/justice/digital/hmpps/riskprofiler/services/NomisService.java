package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Assault;
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeList;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class NomisService {
    private static final ParameterizedTypeReference<List<EscapeList>> ESCAPE_LIST = new ParameterizedTypeReference<>() {};

    private static String URI_ESCAPE_LIST = "/escape-list/{offenderNo}";

    private final RestCallHelper restCallHelper;

    public NomisService(RestCallHelper restCallHelper) {
        this.restCallHelper = restCallHelper;
    }

    public Optional<EscapeList> getEscapeList(String nomsId) {
        log.info("Getting escape list for noms id {}", nomsId);
        URI uri = new UriTemplate(URI_ESCAPE_LIST).expand(nomsId);

        List<EscapeList> escapeList = restCallHelper.getForList(uri, ESCAPE_LIST).getBody();
        return Optional.ofNullable(escapeList.size() > 0 ? escapeList.get(0) : null);
    }

    public List<Alert> getAlertsForOffender(String nomsId, String alertType) {
        return Collections.emptyList();
    }

    public List<Assault> getAssaults(String nomsId) {
        return Collections.emptyList();
    }
}

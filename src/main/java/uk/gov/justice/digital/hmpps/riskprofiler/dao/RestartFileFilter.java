package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.services.DataService;

@Component
public class RestartFileFilter implements GenericFileFilter {
    private final DataService dataService;

    public RestartFileFilter(DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public boolean accept(GenericFile file) {
        return dataService.isCanBeReprocessed(file.getFileName());
    }
}

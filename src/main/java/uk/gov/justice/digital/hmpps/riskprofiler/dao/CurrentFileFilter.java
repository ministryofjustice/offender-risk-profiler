package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.springframework.stereotype.Component;

@Component
public class CurrentFileFilter implements GenericFileFilter {

    private final DataRepository dataRepository;


    public CurrentFileFilter(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public boolean accept(GenericFile file) {
        return dataRepository.isCanBeArchived(file.getFileName());
    }


}

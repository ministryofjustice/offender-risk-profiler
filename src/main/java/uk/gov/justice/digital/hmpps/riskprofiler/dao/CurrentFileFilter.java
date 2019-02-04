package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.springframework.stereotype.Component;

@Component
public class CurrentFileFilter implements GenericFileFilter
{

    private final DataRepository dataRepository;


    public CurrentFileFilter(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public boolean accept(GenericFile file) {

        if (file.getFileName().equalsIgnoreCase(dataRepository.getOcgmData().getFileName())) {
            return false;
        }
        if (file.getFileName().equalsIgnoreCase(dataRepository.getPathfinderData().getFileName())) {
            return false;
        }
        if (file.getFileName().equalsIgnoreCase(dataRepository.getPathfinderData().getFileName())) {
            return false;
        }

        return true;
    }
}

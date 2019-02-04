package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.springframework.stereotype.Component;

@Component
public class RestartFileFilter implements GenericFileFilter {
    private final DataRepository dataRepository;

    public RestartFileFilter(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public boolean accept(GenericFile file) {

        boolean process = false;
        switch (DataRepository.getFileType(file.getFileName())) {

            case PRAS:
                process = dataRepository.getPrasData().getFileName() == null;
                break;
            case OCGM:
                process = dataRepository.getOcgmData().getFileName() == null;
                break;
            case PATHFINDER:
                process = dataRepository.getPathfinderData().getFileName() == null;
                break;
            default:
                break;
        }

        return process;
    }
}

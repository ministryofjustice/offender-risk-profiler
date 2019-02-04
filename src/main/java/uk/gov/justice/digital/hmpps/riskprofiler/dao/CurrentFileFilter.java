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

        boolean process = false;
        switch (DataRepository.getFileType(file.getFileName())) {

            case PRAS:
                process = !(dataRepository.getPrasData() != null && file.getFileName().equalsIgnoreCase(dataRepository.getPrasData().getFileName()));
                break;
            case OCGM:
                process = !(dataRepository.getOcgmData() != null && file.getFileName().equalsIgnoreCase(dataRepository.getOcgmData().getFileName()));
                break;
            case PATHFINDER:
                process = !(dataRepository.getPathfinderData() != null && file.getFileName().equalsIgnoreCase(dataRepository.getPathfinderData().getFileName()));
                break;
            default:
                break;
        }

        return process;
    }
}

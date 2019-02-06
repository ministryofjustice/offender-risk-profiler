package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.OcgmRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PathfinderRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrasRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DataService {

    private final OcgmRepository ocgmRepository;
    private final PathfinderRepository pathfinderRepository;
    private final PrasRepository prasRepository;

    public DataService(OcgmRepository ocgmRepository, PathfinderRepository pathfinderRepository, PrasRepository prasRepository) {
        this.ocgmRepository = ocgmRepository;
        this.pathfinderRepository = pathfinderRepository;
        this.prasRepository = prasRepository;
    }

    public boolean isCanBeReprocessed(String fileName) {
        boolean process = false;
        switch (DataService.getFileType(fileName)) {

            case PRAS:
                process = prasRepository.isCanBeReprocessed();
                break;
            case OCGM:
                process =  ocgmRepository.isCanBeReprocessed();
                break;
            case PATHFINDER:
                process =  pathfinderRepository.isCanBeReprocessed();
                break;
            default:
                break;
        }

        return process;
    }

    public boolean isCanBeArchived(String fileName) {
        boolean process = false;
        switch (DataService.getFileType(fileName)) {

            case PRAS:
                process = prasRepository.isCanBeArchived(fileName);
                break;
            case OCGM:
                process = ocgmRepository.isCanBeArchived(fileName);
                break;
            case PATHFINDER:
                process = pathfinderRepository.isCanBeArchived(fileName);
                break;
            default:
                break;
        }

        return process;
    }


    public void populateData(List<List<String>> csvData, String filename, LocalDateTime timestamp) {

        boolean skipProcessing = false;
        boolean processedFile = false;

        switch (getFileType(filename)) {

            case PRAS:
                skipProcessing = prasRepository.process(csvData, filename, timestamp);
                processedFile = true;
                break;
            case OCGM:
                skipProcessing = ocgmRepository.process(csvData, filename, timestamp);
                processedFile = true;
                break;
            case PATHFINDER:
                skipProcessing = pathfinderRepository.process(csvData, filename, timestamp);
                processedFile = true;
                break;
        }


        if (skipProcessing) {
            log.warn("File {} skipped", filename);
        } else if (processedFile) {
            log.info("Processed {}", filename);
        } else {
            log.warn("Unknown file {}", filename);
        }

    }

    private static FileType getFileType(String filename) {
        if (StringUtils.startsWithIgnoreCase(filename, "OCGM")) {
            return FileType.OCGM;
        }
        if (StringUtils.startsWithIgnoreCase(filename, "PATHFINDER")) {
            return FileType.PATHFINDER;
        }
        if (StringUtils.startsWithIgnoreCase(filename, "PRAS")) {
            return FileType.PRAS;
        }
        return FileType.UNKNOWN;
    }

}

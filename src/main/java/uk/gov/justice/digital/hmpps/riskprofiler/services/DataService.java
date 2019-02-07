package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.*;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DataService {

    private final OcgmRepository ocgmRepository;
    private final OcgRepository ocgRepository;
    private final PathfinderRepository pathfinderRepository;
    private final PrasRepository prasRepository;
    private final ViperRepository viperRepository;

    public DataService(OcgmRepository ocgmRepository, OcgRepository ocgRepository, PathfinderRepository pathfinderRepository, PrasRepository prasRepository, ViperRepository viperRepository) {
        this.ocgmRepository = ocgmRepository;
        this.ocgRepository = ocgRepository;
        this.pathfinderRepository = pathfinderRepository;
        this.prasRepository = prasRepository;
        this.viperRepository = viperRepository;
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
            case OCG:
                process =  ocgRepository.isCanBeReprocessed();
                break;
            case PATHFINDER:
                process =  pathfinderRepository.isCanBeReprocessed();
                break;
            case VIPER:
                process =  viperRepository.isCanBeReprocessed();
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
            case OCG:
                process = ocgRepository.isCanBeArchived(fileName);
                break;
            case PATHFINDER:
                process = pathfinderRepository.isCanBeArchived(fileName);
                break;
            case VIPER:
                process =  viperRepository.isCanBeArchived(fileName);
                break;
            default:
                break;
        }

        return process;
    }


    public void populateData(List<List<String>> csvData, String filename, FileType fileType, LocalDateTime timestamp) {

        boolean skipProcessing = false;
        boolean processedFile = false;

        switch (fileType) {

            case PRAS:
                skipProcessing = prasRepository.process(csvData, filename, timestamp);
                processedFile = true;
                break;
            case OCGM:
                skipProcessing = ocgmRepository.process(csvData, filename, timestamp);
                processedFile = true;
                break;
            case OCG:
                skipProcessing = ocgRepository.process(csvData, filename, timestamp);
                processedFile = true;
                break;
            case PATHFINDER:
                skipProcessing = pathfinderRepository.process(csvData, filename, timestamp);
                processedFile = true;
                break;
            case VIPER:
                skipProcessing = viperRepository.process(csvData, filename, timestamp);
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

    public static FileType getFileType(String filename) {
        if (StringUtils.contains(StringUtils.upperCase(filename), "OCGM")) {
            return FileType.OCGM;
        }
        if (StringUtils.contains(StringUtils.upperCase(filename), "OCG")) {
            return FileType.OCG;
        }
        if (StringUtils.contains(StringUtils.upperCase(filename), "PATHFINDER")) {
            return FileType.PATHFINDER;
        }
        if (StringUtils.contains(StringUtils.upperCase(filename), "PRAS")) {
            return FileType.PRAS;
        }
        if (StringUtils.contains(StringUtils.upperCase(filename), "VIPER")) {
            return FileType.VIPER;
        }
        return FileType.UNKNOWN;
    }

}

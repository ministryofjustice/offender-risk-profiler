package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.*;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DataService {

    private final DataRepository<Ocgm> ocgmRepository;
    private final DataRepository<Ocg> ocgRepository;
    private final DataRepository<PathFinder> pathfinderRepository;
    private final DataRepository<Pras> prasRepository;
    private final DataRepository<Viper> viperRepository;

    public DataService(OcgmRepository ocgmRepository, OcgRepository ocgRepository, PathfinderRepository pathfinderRepository, PrasRepository prasRepository, ViperRepository viperRepository) {
        this.ocgmRepository = ocgmRepository;
        this.ocgRepository = ocgRepository;
        this.pathfinderRepository = pathfinderRepository;
        this.prasRepository = prasRepository;
        this.viperRepository = viperRepository;
    }

    public void populateData(List<List<String>> csvData, String filename, FileType fileType, LocalDateTime timestamp) {

        boolean skipProcessing = true;

        switch (fileType) {

            case PRAS:
                skipProcessing = isSkipProcessing(prasRepository, timestamp);
                if (!skipProcessing) {
                    prasRepository.process(csvData, filename, timestamp);
                }
                break;
            case OCGM:
                skipProcessing = isSkipProcessing(ocgmRepository, timestamp);
                if (!skipProcessing) {
                    ocgmRepository.process(csvData, filename, timestamp);
                }
                break;
            case OCG:
                skipProcessing = isSkipProcessing(ocgRepository, timestamp);
                if (!skipProcessing) {
                    ocgRepository.process(csvData, filename, timestamp);
                }
                break;
            case PATHFINDER:
                skipProcessing = isSkipProcessing(pathfinderRepository, timestamp);
                if (!skipProcessing) {
                    pathfinderRepository.process(csvData, filename, timestamp);
                }
                break;
            case VIPER:
                skipProcessing = isSkipProcessing(viperRepository, timestamp);
                if (!skipProcessing) {
                    viperRepository.process(csvData, filename, timestamp);
                }
                break;
        }


        if (skipProcessing) {
            log.warn("File {} skipped", filename);
        } else {
            log.info("Processed {}", filename);
        }

    }

    private boolean isSkipProcessing(DataRepository<?> data, LocalDateTime timestamp) {
        return data.getFileTimestamp() != null && data.getFileTimestamp().compareTo(timestamp) >= 0;
    }
}

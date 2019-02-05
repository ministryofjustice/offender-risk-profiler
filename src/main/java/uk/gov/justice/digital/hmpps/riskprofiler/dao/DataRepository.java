package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@Slf4j
public class DataRepository {

    private final ImportedFile<Pras> prasData = new ImportedFile<>();
    private final ImportedFile<PathFinder> pathfinderData = new ImportedFile<>();
    private final ImportedFile<Ocgm> ocgmData = new ImportedFile<>();

    public boolean isCanBeReprocessed(String fileName) {
        boolean process = false;
        switch (DataRepository.getFileType(fileName)) {

            case PRAS:
                process = getPrasData().getFileName() == null;
                break;
            case OCGM:
                process = getOcgmData().getFileName() == null;
                break;
            case PATHFINDER:
                process = getPathfinderData().getFileName() == null;
                break;
            default:
                break;
        }

        return process;
    }

    public boolean isCanBeArchived(String fileName) {
        boolean process = false;
        switch (DataRepository.getFileType(fileName)) {

            case PRAS:
                process = getPrasData().getFileName() != null && !fileName.equalsIgnoreCase(getPrasData().getFileName());
                break;
            case OCGM:
                process = getOcgmData().getFileName() != null && !fileName.equalsIgnoreCase(getOcgmData().getFileName());
                break;
            case PATHFINDER:
                process = getPathfinderData().getFileName() != null && !fileName.equalsIgnoreCase(getPathfinderData().getFileName());
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
                skipProcessing = processPras(csvData, filename, timestamp, prasData);
                processedFile = true;
                break;
            case OCGM:
                skipProcessing = processOcgm(csvData, filename, timestamp, ocgmData);
                processedFile = true;
                break;
            case PATHFINDER:
                skipProcessing = processPathfinder(csvData, filename, timestamp, pathfinderData);
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

    static FileType getFileType(String filename) {
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


    private boolean processPathfinder(List<List<String>> csvData, final String filename, final LocalDateTime timestamp, final ImportedFile<PathFinder> dataSet) {
        boolean skipProcessing = dataSet.getFileTimestamp() != null && dataSet.getFileTimestamp().isAfter(timestamp);

        if (!skipProcessing) {
            dataSet.setFileTimestamp(timestamp);
            dataSet.setFileName(filename);

            var map = new HashMap<String, PathFinder>();
            var index = new AtomicInteger(0);
            csvData.stream().filter(p -> index.getAndIncrement() > 0)
                    .forEach(p -> {
                        var pathFinderLine = PathFinder.builder()
                                .nomisId(p.get(PathFinder.NOMIS_ID_POSITION))
                                .pathFinderBanding(p.get(PathFinder.PATH_FINDER_BINDING_POSITION))
                                .build();

                        if (map.put(pathFinderLine.getNomisId(), pathFinderLine) != null) {
                            log.warn("Duplicate key found in PathFinder {}", p);
                        }
                    });
            dataSet.setDataSet(map);
        }
        return skipProcessing;

    }

    private boolean processOcgm(List<List<String>> csvData, final String filename, final LocalDateTime timestamp, final ImportedFile<Ocgm> dataSet) {
        boolean skipProcessing = dataSet.getFileTimestamp() != null && dataSet.getFileTimestamp().compareTo(timestamp) >= 0;

        if (!skipProcessing) {
            dataSet.setFileTimestamp(timestamp);
            dataSet.setFileName(filename);

            var map = new HashMap<String, Ocgm>();
            var index = new AtomicInteger(0);
            csvData.stream().filter(p -> index.getAndIncrement() > 0)
                    .forEach(p -> {
                        var ocgmLine = Ocgm.builder()
                                .nomisId(p.get(Ocgm.NOMIS_ID_POSITION))
                                .ocgmBand(p.get(Ocgm.OCGM_BAND_POSITION))
                                .standingWithinOcg(p.get(Ocgm.STANDING_POSITION))
                                .build();

                        if (map.put(ocgmLine.getNomisId(), ocgmLine) != null) {
                            log.warn("Duplicate key found in OCGM Data {}", p);
                        }
                    });
            dataSet.setDataSet(map);
        }
        return skipProcessing;

    }

    private boolean processPras(List<List<String>> csvData, final String filename, final LocalDateTime timestamp, final ImportedFile<Pras> dataSet) {
        boolean skipProcessing = dataSet.getFileTimestamp() != null && dataSet.getFileTimestamp().isAfter(timestamp);

        if (!skipProcessing) {
            dataSet.setFileTimestamp(timestamp);
            dataSet.setFileName(filename);

            var map = new HashMap<String, Pras>();
            var index = new AtomicInteger(0);
            csvData.stream().filter(p -> index.getAndIncrement() > 0)
                    .forEach(p -> {
                        var prasLine = Pras.builder().nomisId(p.get(Pras.NOMIS_ID_POSITION)).build();

                        if (map.put(prasLine.getNomisId(), prasLine) != null) {
                            log.warn("Duplicate key found in PRAS Data {}", p);
                        }
                    });
            dataSet.setDataSet(map);
        }
        return skipProcessing;

    }

    public Optional<Ocgm> getOcgmDataByNomsId(String nomsId) {
        return Optional.ofNullable(ocgmData.getDataSet().get(nomsId));
    }

    public Optional<PathFinder> getPathfinderDataByNomsId(String nomsId) {
        return Optional.ofNullable(pathfinderData.getDataSet().get(nomsId));
    }

    public Optional<Pras> getPrasDataByNomsId(String nomsId) {
        return Optional.ofNullable(prasData.getDataSet().get(nomsId));
    }

    public ImportedFile<Pras> getPrasData() {
        return prasData;
    }

    public ImportedFile<PathFinder> getPathfinderData() {
        return pathfinderData;
    }

    public ImportedFile<Ocgm> getOcgmData() {
        return ocgmData;
    }
}

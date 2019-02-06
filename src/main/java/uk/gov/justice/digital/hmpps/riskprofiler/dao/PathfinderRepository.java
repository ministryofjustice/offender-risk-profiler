package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@Slf4j
public class PathfinderRepository {

    private final ImportedFile<PathFinder> pathfinderData = new ImportedFile<>();

    public boolean isCanBeReprocessed() {
        return pathfinderData.getFileName() == null;
    }

    public boolean isCanBeArchived(String fileName) {
        return pathfinderData.getFileName() != null && !fileName.equalsIgnoreCase(pathfinderData.getFileName());
    }


    public boolean process(List<List<String>> csvData, final String filename, final LocalDateTime timestamp) {
        boolean skipProcessing = pathfinderData.getFileTimestamp() != null && pathfinderData.getFileTimestamp().isAfter(timestamp);

        if (!skipProcessing) {
            pathfinderData.setFileTimestamp(timestamp);
            pathfinderData.setFileName(filename);

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
            pathfinderData.setDataSet(map);
        }
        return skipProcessing;

    }
    public Optional<PathFinder> getPathfinderDataByNomsId(String nomsId) {
        return Optional.ofNullable(pathfinderData.getDataSet().get(nomsId));
    }

}

package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@Slf4j
public class PrasRepository {

    private final ImportedFile<Pras> prasData = new ImportedFile<>();

    public boolean isCanBeReprocessed() {
        return prasData.getFileName() == null;
    }

    public boolean isCanBeArchived(String fileName) {
        return prasData.getFileName() != null && !fileName.equalsIgnoreCase(prasData.getFileName());
    }

    private static FileType getFileType(String filename) {
        if (StringUtils.startsWithIgnoreCase(filename, "OCGM")) {
            return FileType.OCGM;
        }
        return FileType.UNKNOWN;
    }


    public boolean process(List<List<String>> csvData, final String filename, final LocalDateTime timestamp) {
        boolean skipProcessing = prasData.getFileTimestamp() != null && prasData.getFileTimestamp().isAfter(timestamp);

        if (!skipProcessing) {
            prasData.setFileTimestamp(timestamp);
            prasData.setFileName(filename);

            var map = new HashMap<String, Pras>();
            var index = new AtomicInteger(0);
            csvData.stream().filter(p -> index.getAndIncrement() > 0)
                    .forEach(p -> {
                        var prasLine = Pras.builder().nomisId(p.get(Pras.NOMIS_ID_POSITION)).build();

                        if (map.put(prasLine.getNomisId(), prasLine) != null) {
                            log.warn("Duplicate key found in PRAS Data {}", p);
                        }
                    });
            prasData.setDataSet(map);
        }
        return skipProcessing;

    }

    public Optional<Pras> getPrasDataByNomsId(String nomsId) {
        return Optional.ofNullable(prasData.getDataSet().get(nomsId));
    }
}

package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@Slf4j
public class OcgmRepository {

    private final ImportedFile<Ocgm> ocgmData = new ImportedFile<>();

    public boolean isCanBeReprocessed() {
        return ocgmData.getFileName() == null;
    }

    public boolean isCanBeArchived(String fileName) {
       return ocgmData.getFileName() != null && !fileName.equalsIgnoreCase(ocgmData.getFileName());
    }

    public boolean process(List<List<String>> csvData, final String filename, final LocalDateTime timestamp) {
        boolean skipProcessing = ocgmData.getFileTimestamp() != null && ocgmData.getFileTimestamp().compareTo(timestamp) >= 0;

        if (!skipProcessing) {
            ocgmData.setFileTimestamp(timestamp);
            ocgmData.setFileName(filename);

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
            ocgmData.setDataSet(map);
        }
        return skipProcessing;

    }

    public Optional<Ocgm> getOcgmDataByNomsId(String nomsId) {
        return Optional.ofNullable(ocgmData.getDataSet().get(nomsId));
    }

}

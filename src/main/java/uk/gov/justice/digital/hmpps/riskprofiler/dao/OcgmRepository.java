package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class OcgmRepository implements DataRepository<Ocgm> {

    private final ImportedFile<Ocgm> data = new ImportedFile<>();

    @Override
    public boolean isCanBeReprocessed() {
        return data.getFileName() == null;
    }

    @Override
    public boolean isCanBeArchived(String fileName) {
       return data.getFileName() != null && !fileName.equalsIgnoreCase(data.getFileName());
    }

    @Override
    public boolean process(List<List<String>> csvData, final String filename, final LocalDateTime timestamp) {
        boolean skipProcessing = data.getFileTimestamp() != null && data.getFileTimestamp().compareTo(timestamp) >= 0;

        if (!skipProcessing) {
            data.setFileTimestamp(timestamp);
            data.setFileName(filename);
            data.reset();

            csvData.stream().filter(p -> data.getIndex().getAndIncrement() > 0)
                    .forEach(p -> {
                        try {
                            final var key = p.get(Ocgm.NOMIS_ID_POSITION);
                            if (StringUtils.isNotBlank(key)) {

                                if (data.getDataSet().get(key) != null) {
                                    log.warn("Duplicate key found in line {} for key {}", data.getIndex().get(), key);
                                    data.getLinesDup().incrementAndGet();
                                } else {
                                    var ocgId = p.get(Ocgm.OCG_ID_POSITION);
                                    if (StringUtils.isBlank(ocgId)) {
                                        log.warn("No OCG Id in line {} for Key {}", data.getIndex().get(), key);
                                        data.getLinesInvalid().incrementAndGet();
                                    } else {
                                        var ocgmLine = Ocgm.builder()
                                                .nomisId(key)
                                                .ocgId(StringUtils.trimToNull(ocgId))
                                                .standingWithinOcg(StringUtils.trimToNull(p.get(Ocgm.STANDING_POSITION)))
                                                .build();

                                        data.getDataSet().put(key, ocgmLine);
                                        data.getLinesProcessed().incrementAndGet();
                                    }
                                }
                            } else {
                                log.warn("Missing key in line {}", data.getIndex().get(), key);
                                data.getLinesInvalid().incrementAndGet();
                            }
                        } catch (Exception e) {
                            log.warn("Error in Line {}", data.getIndex().get(), p);
                            data.getLinesError().incrementAndGet();
                        }
                    });
            log.info("Lines total {}, processed {}, dups {}, invalid {}, errors {}", data.getIndex().get(),
                    data.getLinesProcessed().get(), data.getLinesDup().get(), data.getLinesInvalid().get(), data.getLinesError().get());

        }
        return skipProcessing;

    }

    @Override
    public Optional<Ocgm> getByKey(String key) {
        return Optional.ofNullable(data.getDataSet().get(key));
    }

}

package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.OcgmList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType.OCGM;

@Repository
@Slf4j
public class OcgmRepository implements DataRepository<OcgmList> {

    private final ImportedFile<OcgmList> data = new ImportedFile<>();

    @Override
    public void process(List<List<String>> csvData, final String filename, final LocalDateTime timestamp) {

        data.setFileTimestamp(timestamp);
        data.setFileName(filename);
        data.setFileType(OCGM);
        data.reset();

        csvData.stream().filter(p -> data.getIndex().getAndIncrement() > 0)
                .forEach(p -> {
                    try {
                        final var key = p.get(Ocgm.NOMIS_ID_POSITION);
                        if (StringUtils.isNotBlank(key)) {
                            if (!NOMS_ID_REGEX.matcher(key).matches()) {
                                log.warn("Invalid Key in line {} for Key {}", data.getIndex().get(), key);
                                data.getLinesInvalid().incrementAndGet();
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

                                    var dataSet = data.getDataSet().get(key);
                                    if (dataSet != null) {
                                        dataSet.getData().add(ocgmLine);
                                    } else {
                                        data.getDataSet().put(key, OcgmList.builder().nomisId(key).ocgm(ocgmLine).build());
                                    }
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

    public ImportedFile<OcgmList> getData() {
        return data;
    }

    public LocalDateTime getFileTimestamp() {
        return data.getFileTimestamp();
    }

    @Override
    public Optional<OcgmList> getByKey(String key) {
        return Optional.ofNullable(data.getDataSet().get(key));
    }

}

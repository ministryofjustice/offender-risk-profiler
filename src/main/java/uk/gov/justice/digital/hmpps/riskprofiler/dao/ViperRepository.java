package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Viper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType.VIPER;

@Repository
@Slf4j
public class ViperRepository extends DataRepository<Viper> {

    @Override
    protected void doProcess(List<List<String>> csvData, final String filename, final LocalDateTime timestamp, final ImportedFile<Viper> data) {

        data.setFileTimestamp(timestamp);
        data.setFileName(filename);
        data.setFileType(VIPER);
        data.reset();

        csvData.forEach(p -> {
                    try {
                        final var key = p.get(Viper.NOMIS_ID_POSITION);
                        if (StringUtils.isNotBlank(key)) {
                            if (!NOMS_ID_REGEX.matcher(key).matches()) {
                                log.warn("Invalid Key in line {} for Key {}", data.getIndex().get(), key);
                                data.getLinesInvalid().incrementAndGet();
                            } else {
                                if (data.getDataSet().get(key) != null) {
                                    log.warn("Duplicate key found in line {} for key {}", data.getIndex().get(), key);
                                    data.getLinesDup().incrementAndGet();
                                } else {
                                    var viperScore = p.get(Viper.SCORE_POSITION);
                                    if (StringUtils.isBlank(viperScore)) {
                                        log.warn("No Score in line {} for Key {}", data.getIndex().get(), key);
                                        data.getLinesInvalid().incrementAndGet();
                                    } else {
                                        var viperLine = Viper.builder()
                                                .nomisId(key)
                                                .score(BigDecimal.valueOf(Math.exp(Double.valueOf(viperScore))))
                                                .build();

                                        data.getDataSet().put(key, viperLine);
                                        data.getLinesProcessed().incrementAndGet();
                                    }
                                }
                            }
                        } else {
                            log.warn("Missing key in line {} key [{}]", data.getIndex().get(), key);
                            data.getLinesInvalid().incrementAndGet();
                        }
                    } catch (Exception e) {
                        log.warn("Error in Line {} data [{}]", data.getIndex().get(), p);
                        data.getLinesError().incrementAndGet();
                    }

            data.getIndex().getAndIncrement();
        });
        log.info("Lines total {}, processed {}, dups {}, invalid {}, errors {}", data.getIndex().get(),
                data.getLinesProcessed().get(), data.getLinesDup().get(), data.getLinesInvalid().get(), data.getLinesError().get());
    }
}

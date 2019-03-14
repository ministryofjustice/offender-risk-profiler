package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.RiskDataSet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public interface DataRepository<F extends RiskDataSet> {

    Pattern NOMS_ID_REGEX = Pattern.compile("^[A-Z]\\d{4}[A-Z]{2}$");

    void process(List<List<String>> csvData, String filename, LocalDateTime timestamp);

    default Optional<F> getByKey(String key) {
        if (getData().getDataSet() != null) {
            return Optional.ofNullable(getData().getDataSet().get(key));
        }
        return Optional.empty();
    }

    LocalDateTime getFileTimestamp();

    ImportedFile<F> getData();
}

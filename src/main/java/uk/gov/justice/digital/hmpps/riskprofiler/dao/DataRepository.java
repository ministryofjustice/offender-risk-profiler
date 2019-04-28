package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.RiskDataSet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@Slf4j
public abstract class DataRepository<F extends RiskDataSet> {

    final Pattern NOMS_ID_REGEX = Pattern.compile("^[A-Z]\\d{4}[A-Z]{2}$");

    private final ImportedFile<F> dataA = new ImportedFile<>();
    private final ImportedFile<F> dataB = new ImportedFile<>();
    private volatile AtomicBoolean isA = new AtomicBoolean(true);

    public void process(List<List<String>> csvData, String filename, LocalDateTime timestamp) {
        doProcess(csvData,  filename,  timestamp, getStandbyData());
        toggleData();
    }

    protected abstract void doProcess(List<List<String>> csvData, String filename, LocalDateTime timestamp, ImportedFile<F> data);

    public Optional<F> getByKey(String key) {
        if (getData().getDataSet() != null) {
            return Optional.ofNullable(getData().getDataSet().get(key));
        }
        return Optional.empty();
    }

    public LocalDateTime getFileTimestamp() {
        return (isA.get() ? dataA : dataB).getFileTimestamp();
    }

    public ImportedFile<F> getData() {
        return isA.get() ? dataA : dataB;
    }

    public ImportedFile<F> getStandbyData() {
        return isA.get() ? dataB : dataA;
    }

    private void toggleData() {
        isA.set(!isA.get());
        log.debug("Switched to {} data map {}", getData().getFileType(), isA.get() ? "A" : "B");
    }
}

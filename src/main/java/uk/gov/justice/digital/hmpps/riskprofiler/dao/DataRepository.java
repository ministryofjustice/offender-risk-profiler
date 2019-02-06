package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.RiskDataSet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DataRepository<F extends RiskDataSet> {
    boolean isCanBeReprocessed();

    boolean isCanBeArchived(String fileName);

    boolean process(List<List<String>> csvData, String filename, LocalDateTime timestamp);

    Optional<F> getByKey(String key);
}
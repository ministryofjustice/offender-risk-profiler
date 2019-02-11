package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ExchangeProperty;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.RiskDataSet;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DataService {

    private final DataRepositoryFactory factory;

    public DataService(DataRepositoryFactory factory) {
        this.factory = factory;
    }

    public void process(List<List<String>> csvData, @ExchangeProperty("fileType") FileType fileType, @ExchangeProperty("fileInfo") PendingFile fileInfo) {

        var repository = factory.getRepository(fileType.getType());
        if (isFileShouldBeProcessed(repository, fileInfo.getFileTimestamp())) {
            repository.process(csvData, fileInfo.getFileName(), fileInfo.getFileTimestamp());
            log.info("Processed {}", fileInfo.getFileName());
        } else {
            log.warn("Skipped {}", fileInfo.getFileName());
        }
    }

    private boolean isFileShouldBeProcessed(DataRepository<? extends RiskDataSet> data, LocalDateTime timestamp) {
        return data.getFileTimestamp() == null || data.getFileTimestamp().compareTo(timestamp) < 0;
    }
}

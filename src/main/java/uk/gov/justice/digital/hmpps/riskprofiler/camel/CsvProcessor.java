package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType;
import uk.gov.justice.digital.hmpps.riskprofiler.services.DataService;

import java.util.List;

import static uk.gov.justice.digital.hmpps.riskprofiler.utils.FileFormatUtils.extractTimestamp;

@Component
@Slf4j
public class CsvProcessor {

    private final DataService dataService;

    public CsvProcessor(DataService dataService) {
        this.dataService = dataService;
    }

    public void doHandleFileCsvData(List<List<String>> csvData, Exchange exchange) {
        String fileName = exchange.getIn().getHeader("fileName", String.class);
        String fileType = exchange.getIn().getHeader("fileType", String.class);
        log.info("Processing file {}", fileName);
        dataService.populateData(csvData, fileName, FileType.valueOf(fileType), extractTimestamp(fileName));
    }


}

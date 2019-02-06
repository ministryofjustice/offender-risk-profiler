package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.services.DataService;

import java.util.List;

import static uk.gov.justice.digital.hmpps.riskprofiler.utils.FileFormatUtils.extractTimestamp;

@Component
@Slf4j
public class CsvProcessor {

    static final String PROCESS_CSV = "direct:process-csv";

    private final DataService dataService;

    public CsvProcessor(DataService dataService) {
        this.dataService = dataService;
    }

    public void doHandleFileCsvData(List<List<String>> csvData, Exchange exchange) {
        process(csvData, exchange.getIn().getHeader("CamelFileName", String.class));
    }

    public void doHandleS3CsvData(List<List<String>> csvData, Exchange exchange) {
        process(csvData, exchange.getIn().getHeader("CamelAwsS3Key", String.class));
    }

    private void process(List<List<String>> csvData, String filename) {
        log.info("Processing file {}", filename);
        dataService.populateData(csvData, filename, extractTimestamp(filename));
    }


}
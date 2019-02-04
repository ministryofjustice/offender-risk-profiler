package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;

@Component
@ConditionalOnProperty(name = "filesystem.enabled")
public class CsvProcessorRoute extends RouteBuilder {

    static final String PROCESS_CSV = "direct:process-csv";
    private final DataRepository csvProcessor;

    public CsvProcessorRoute(DataRepository csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    @Override
    public void configure() {

        from(PROCESS_CSV)
                .unmarshal().csv()
                .bean(csvProcessor, "doHandleCsvData");

    }
}

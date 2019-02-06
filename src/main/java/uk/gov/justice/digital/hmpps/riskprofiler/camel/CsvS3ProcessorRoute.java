package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static uk.gov.justice.digital.hmpps.riskprofiler.camel.CsvProcessor.PROCESS_CSV;

@Component
@ConditionalOnProperty(name = "file.process.type", havingValue = "s3")
public class CsvS3ProcessorRoute extends RouteBuilder {

    private final CsvProcessor csvProcessor;

    public CsvS3ProcessorRoute(CsvProcessor csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    @Override
    public void configure() {

        from(PROCESS_CSV)
                .unmarshal().csv()
                .bean(csvProcessor, "doHandleS3CsvData");
    }
}

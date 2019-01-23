package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;

@Component
@ConditionalOnProperty(name = "filesystem.enabled")
public class FileCsvProcessorRoute extends RouteBuilder {

    private final DataRepository csvProcessor;

    public FileCsvProcessorRoute(DataRepository csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    @Override
    public void configure() {

        from("file:src/test/resources/?noop=true")
                .unmarshal().csv()
                .bean(csvProcessor, "doHandleCsvData");
    }
}

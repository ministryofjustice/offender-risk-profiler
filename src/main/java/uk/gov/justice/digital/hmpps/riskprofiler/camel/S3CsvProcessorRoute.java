package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;

@Component
@ConditionalOnProperty(name = "aws.s3.enabled")
public class S3CsvProcessorRoute extends RouteBuilder {

    private final DataRepository csvProcessor;

    public S3CsvProcessorRoute(DataRepository csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    @Override
    public void configure() {

        from("aws-s3://risk-profile-test?amazonS3Client=#s3client&delay=5000&maxMessagesPerPoll=10&deleteAfterRead=false&noop=true")
                .unmarshal().csv()
                .bean(csvProcessor, "doHandleCsvData");
    }
}

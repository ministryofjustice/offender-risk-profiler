package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.services.S3Service;

import static uk.gov.justice.digital.hmpps.riskprofiler.camel.CsvProcessor.PROCESS_CSV;


@Component
@ConditionalOnProperty(name = "file.process.type", havingValue = "s3")
public class PathfinderS3CsvProcessorRoute extends RouteBuilder {

    private final S3Service s3Service;

    public PathfinderS3CsvProcessorRoute(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Override
    public void configure() {

        from("aws-s3://{{s3.bucket.pathfinder}}?amazonS3Client=#s3client&delay=5000")
                .setProperty("baseBucketName", simple("{{s3.bucket.pathfinder}}"))
                .bean(s3Service, "moveToPending");

        from("aws-s3://{{s3.bucket.pathfinder}}-pending?amazonS3Client=#s3client&delay=5000")
                .convertBodyTo(byte[].class)
                .setHeader("pendingFile", body())
                .to(PROCESS_CSV)
                .setBody(header("pendingFile"))
                .setProperty("baseBucketName", simple("{{s3.bucket.pathfinder}}"))
                .bean(s3Service, "moveToProcessed");

        from("aws-s3://{{s3.bucket.pathfinder}}-processed?amazonS3Client=#s3client&delay=10000&deleteAfterRead=false")
                .setProperty("baseBucketName", simple("{{s3.bucket.pathfinder}}"))
                .bean(s3Service, "resetFile");
    }
}

package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static uk.gov.justice.digital.hmpps.riskprofiler.camel.CsvProcessor.PROCESS_CSV;

@Component
@ConditionalOnProperty(name = "file.process.type", havingValue = "file")
public class PathfinderCsvProcessorRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("file:src/test/resources/buckets/pathfinder?move=pending/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}&moveFailed=../error&delay=5000")
                .log("Moved ${file:name}");

        from("file:src/test/resources/buckets/pathfinder/processed?filter=#restartFileFilter&move=../pending&delay=10000")
                .log("Move to pending ${file:name}");

        from("file:src/test/resources/buckets/pathfinder/processed?filter=#currentFileFilter&move=../archive&delay=30000&initialDelay=30000")
                .log("Archived ${file:name}");

        from("file:src/test/resources/buckets/pathfinder/pending?move=../processed&moveFailed=../error")
                .setHeader("dataFileType", simple("PATHFINDER"))
                .to(PROCESS_CSV);

    }
}

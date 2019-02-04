package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;

@Component
@ConditionalOnProperty(name = "filesystem.enabled")
public class FileCsvProcessorRoute extends RouteBuilder {

    public static final String PROCESS_CSV = "direct:process-csv";
    private final DataRepository csvProcessor;

    public FileCsvProcessorRoute(DataRepository csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    @Override
    public void configure() {

        from("file:src/test/resources/buckets/ocgm?preMove=pending/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}&move=../processed&moveFailed=../error")
                .to(PROCESS_CSV);


        from("file:src/test/resources/buckets/ocgm/processed?noop=true")
                .to(PROCESS_CSV);


        from("file:src/test/resources/buckets/pathfinder?preMove=pending/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}&move=../processed&moveFailed=../error")
                .to(PROCESS_CSV);


        from("file:src/test/resources/buckets/pathfinder/processed?noop=true")
                .to(PROCESS_CSV);


        from("file:src/test/resources/buckets/pras?preMove=pending/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}&move=../processed&moveFailed=../error")
                .to(PROCESS_CSV);


        from("file:src/test/resources/buckets/pras/processed?noop=true")
                .to(PROCESS_CSV);

        from(PROCESS_CSV)
                .unmarshal().csv()
                .bean(csvProcessor, "doHandleCsvData");

        from("file:src/test/resources/buckets/pras/processed?filter=#currentFileFilter&move=../archive")
            .log("Archived");

        from("file:src/test/resources/buckets/ocgm/processed?filter=#currentFileFilter&move=../archive")
                .log("Archived");

        from("file:src/test/resources/buckets/pathfinder/processed?filter=#currentFileFilter&move=../archive")
                .log("Archived");

    }
}

package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.services.DataService;
import uk.gov.justice.digital.hmpps.riskprofiler.services.FileService;

@Component
public class CsvProcessorRoute extends RouteBuilder {

    private final DataService dataService;
    private final FileService fileService;

    public CsvProcessorRoute(DataService dataService, FileService fileService) {
        this.dataService = dataService;
        this.fileService = fileService;
    }

    @Override
    public void configure() {

        getContext().setStreamCaching(true);

        from("timer://pathfinder-schedule?fixedRate=true&period={{pathfinder.period}}")
                .bean(fileService, "getLatestFile('{{s3.bucket.pathfinder}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setProperty("fileInfo", simple("${body}"))
                        .setProperty("fileType", simple("PATHFINDER"))
                        .setBody(simple("${body.data}"))
                        .unmarshal().csv()
                        .bean(dataService, "process")
                .endChoice();

        from("timer://pras-schedule?fixedRate=true&period={{pras.period}}")
                .bean(fileService, "getLatestFile('{{s3.bucket.pras}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setProperty("fileInfo", simple("${body}"))
                        .setProperty("fileType", simple("PRAS"))
                        .setBody(simple("${body.data}"))
                        .unmarshal().csv()
                        .bean(dataService, "process")
                .endChoice();

        from("timer://ocgm-schedule?fixedRate=true&period={{ocgm.period}}")
                .bean(fileService, "getLatestFile('{{s3.bucket.ocgm}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setProperty("fileInfo", simple("${body}"))
                        .setProperty("fileType", simple("OCGM"))
                        .setBody(simple("${body.data}"))
                        .unmarshal().csv()
                        .bean(dataService, "process")
                .endChoice();

        from("timer://ocg-schedule?fixedRate=true&period={{ocg.period}}")
                .bean(fileService, "getLatestFile('{{s3.bucket.ocg}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setProperty("fileInfo", simple("${body}"))
                        .setProperty("fileType", simple("OCG"))
                        .setBody(simple("${body.data}"))
                        .unmarshal().csv()
                        .bean(dataService, "process")
                .endChoice();

        from("timer://viper-schedule?fixedRate=true&period={{viper.period}}")
                .bean(fileService, "getLatestFile('{{s3.bucket.viper}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setProperty("fileInfo", simple("${body}"))
                        .setProperty("fileType", simple("VIPER"))
                        .setBody(simple("${body.data}"))
                        .unmarshal().csv()
                        .bean(dataService, "process")
                .endChoice();


    }
}

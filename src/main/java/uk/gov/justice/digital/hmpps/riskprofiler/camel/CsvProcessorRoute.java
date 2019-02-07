package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.services.FileService;

@Component
public class CsvProcessorRoute extends RouteBuilder {

    private final CsvProcessor csvProcessor;
    private final FileService fileService;

    public CsvProcessorRoute(CsvProcessor csvProcessor, FileService fileService) {
        this.csvProcessor = csvProcessor;
        this.fileService = fileService;
    }

    @Override
    public void configure() {

        getContext().setStreamCaching(true);

        from("scheduler://pathfinder-schedule?scheduler=spring&scheduler.cron={{pathfinder.cron}}")
                .bean(fileService, "getLatestFile('{{s3.bucket.pathfinder}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setHeader("fileName", simple("${body.fileName}"))
                        .setHeader("fileType", simple("PATHFINDER"))
                        .setBody(simple("${body.data}"))
                .unmarshal().csv()
                        .bean(csvProcessor, "doHandleFileCsvData")
                .endChoice();

        from("scheduler://pras-schedule?scheduler=spring&scheduler.cron={{pras.cron}}")
                .bean(fileService, "getLatestFile('{{s3.bucket.pras}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setHeader("fileName", simple("${body.fileName}"))
                        .setHeader("fileType", simple("PRAS"))
                        .setBody(simple("${body.data}"))
                .unmarshal().csv()
                        .bean(csvProcessor, "doHandleFileCsvData")
                .endChoice();

        from("scheduler://ocgm-schedule?scheduler=spring&scheduler.cron={{ocgm.cron}}")
                .bean(fileService, "getLatestFile('{{s3.bucket.ocgm}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setHeader("fileName", simple("${body.fileName}"))
                        .setHeader("fileType", simple("OCGM"))
                        .setBody(simple("${body.data}"))
                .unmarshal().csv()
                        .bean(csvProcessor, "doHandleFileCsvData")
                .endChoice();

        from("scheduler://ocg-schedule?scheduler=spring&scheduler.cron={{ocg.cron}}")
                .bean(fileService, "getLatestFile('{{s3.bucket.ocg}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setHeader("fileName", simple("${body.fileName}"))
                        .setHeader("fileType", simple("OCG"))
                        .setBody(simple("${body.data}"))
                .unmarshal().csv()
                        .bean(csvProcessor, "doHandleFileCsvData")
                .endChoice();

        from("scheduler://viper-schedule?scheduler=spring&scheduler.cron={{viper.cron}}")
                .bean(fileService, "getLatestFile('{{s3.bucket.viper}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setHeader("fileName", simple("${body.fileName}"))
                        .setHeader("fileType", simple("VIPER"))
                        .setBody(simple("${body.data}"))
                        .unmarshal().csv()
                        .bean(csvProcessor, "doHandleFileCsvData")
                .endChoice();


    }
}

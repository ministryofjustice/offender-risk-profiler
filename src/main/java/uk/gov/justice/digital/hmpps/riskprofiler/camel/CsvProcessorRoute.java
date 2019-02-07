package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.services.S3Service;

@Component
@ConditionalOnProperty(name = "file.process.type", havingValue = "s3")
public class CsvProcessorRoute extends RouteBuilder {

    private final CsvProcessor csvProcessor;
    private final S3Service s3Service;

    public CsvProcessorRoute(CsvProcessor csvProcessor, S3Service s3Service) {
        this.csvProcessor = csvProcessor;
        this.s3Service = s3Service;
    }

    @Override
    public void configure() {

        getContext().setStreamCaching(true);

        from("scheduler://pathfinder-schedule?scheduler=spring&scheduler.cron={{pathfinder.cron}}")
                .bean(s3Service, "getLatestFile('{{s3.bucket.pathfinder}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setHeader("fileName", simple("${body.name}"))
                        .setHeader("fileType", simple("PATHFINDER"))
                        .unmarshal().csv()
                        .bean(csvProcessor, "doHandleFileCsvData")
                .endChoice();

        from("scheduler://pras-schedule?scheduler=spring&scheduler.cron={{pras.cron}}")
                .bean(s3Service, "getLatestFile('{{s3.bucket.pras}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setHeader("fileName", simple("${body.name}"))
                        .setHeader("fileType", simple("PRAS"))
                        .unmarshal().csv()
                        .bean(csvProcessor, "doHandleFileCsvData")
                .endChoice();

        from("scheduler://ocgm-schedule?scheduler=spring&scheduler.cron={{ocgm.cron}}")
                .bean(s3Service, "getLatestFile('{{s3.bucket.ocgm}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setHeader("fileName", simple("${body.name}"))
                        .setHeader("fileType", simple("OCGM"))
                        .unmarshal().csv()
                        .bean(csvProcessor, "doHandleFileCsvData")
                .endChoice();

        from("scheduler://ocg-schedule?scheduler=spring&scheduler.cron={{ocg.cron}}")
                .bean(s3Service, "getLatestFile('{{s3.bucket.ocg}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setHeader("fileName", simple("${body.name}"))
                        .setHeader("fileType", simple("OCG"))
                        .unmarshal().csv()
                        .bean(csvProcessor, "doHandleFileCsvData")
                .endChoice();

        from("scheduler://viper-schedule?scheduler=spring&scheduler.cron={{viper.cron}}")
                .bean(s3Service, "getLatestFile('{{s3.bucket.viper}}')")
                .choice()
                    .when().simple("${body} != null")
                        .setHeader("fileName", simple("${body.name}"))
                        .setHeader("fileType", simple("VIPER"))
                        .unmarshal().csv()
                        .bean(csvProcessor, "doHandleFileCsvData")
                .endChoice();


    }
}

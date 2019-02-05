package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static uk.gov.justice.digital.hmpps.riskprofiler.utils.FileFormatUtils.createTimestampFile;

@Component
@Slf4j
@ConditionalOnProperty(name = "file.process.type", havingValue = "s3")
public class S3Service {

    private final AmazonS3 s3client;
    private final DataRepository dataRepository;

    public S3Service(AmazonS3 s3client, DataRepository dataRepository) {
        this.s3client = s3client;
        this.dataRepository = dataRepository;
    }

    public void moveToPending(InputStream stream, Exchange exchange) throws IOException {
        var filename = exchange.getIn().getHeader("CamelAwsS3Key", String.class);

        String timestampFile = createTimestampFile(filename);
        moveFile(stream, "risk-profile-ocgm-pending", timestampFile);
        log.info("Moved to pending {}", timestampFile);
    }

    public void moveToProcessed(InputStream stream, Exchange exchange) throws IOException {
        var filename = exchange.getIn().getHeader("CamelAwsS3Key", String.class);
        moveFile(stream, "risk-profile-ocgm-processed", filename);
        log.info("Moved to processed {}", filename);
    }

    public void resetFile(InputStream stream, Exchange exchange) throws IOException {
        var filename = exchange.getIn().getHeader("CamelAwsS3Key", String.class);

        if (dataRepository.isCanBeArchived(filename)) {
            log.info("Moving {} to archive", filename);
            moveFile(stream, "risk-profile-ocgm-archive", filename);
            s3client.deleteObject("risk-profile-ocgm-processed", filename);
        } else if (dataRepository.isCanBeReprocessed(filename)) {
            log.info("Moving {} to pending", filename);
            moveFile(stream, "risk-profile-ocgm-pending", filename);
            s3client.deleteObject("risk-profile-ocgm-processed", filename);
        }
        IOUtils.drainInputStream(stream);
    }

    private void moveFile(InputStream stream, String bucketName, String filename) throws IOException {
        final var targetFile = new File(filename);
        final var outStream = new FileOutputStream(targetFile);
        outStream.write(IOUtils.toByteArray(stream));
        s3client.putObject(bucketName, filename, targetFile);
    }

}

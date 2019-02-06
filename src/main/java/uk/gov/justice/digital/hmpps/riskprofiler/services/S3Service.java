package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

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
    private final DataService dataService;

    public S3Service(AmazonS3 s3client, DataService dataService) {
        this.s3client = s3client;
        this.dataService = dataService;
    }

    public void moveToPending(InputStream stream, Exchange exchange) throws IOException {
        var filename = exchange.getIn().getHeader("CamelAwsS3Key", String.class);
        var baseBucketName = exchange.getProperty("baseBucketName", String.class);

        String timestampFile = createTimestampFile(filename);
        moveFile(stream, baseBucketName+"-pending", timestampFile);
        log.info("Moved to pending {}", timestampFile);
    }

    public void moveToProcessed(InputStream stream, Exchange exchange) throws IOException {
        var filename = exchange.getIn().getHeader("CamelAwsS3Key", String.class);
        var baseBucketName = exchange.getProperty("baseBucketName", String.class);
        moveFile(stream, baseBucketName+"-processed", filename);
        log.info("Moved to processed {}", filename);
    }

    public void resetFile(InputStream stream, Exchange exchange) throws IOException {
        var filename = exchange.getIn().getHeader("CamelAwsS3Key", String.class);
        var baseBucketName = exchange.getProperty("baseBucketName", String.class);

        boolean fileMoved = false;
        if (dataService.isCanBeArchived(filename)) {
            log.info("Moving {} to archive", filename);
            moveFile(stream, baseBucketName+"-archive", filename);
            fileMoved = true;
        } else if (dataService.isCanBeReprocessed(filename)) {
            log.info("Moving {} to pending", filename);
            moveFile(stream, baseBucketName+"-pending", filename);
            fileMoved = true;
        }

        if (fileMoved) {
            s3client.deleteObject(baseBucketName+"-processed", filename);
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
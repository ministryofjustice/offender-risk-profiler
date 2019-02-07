package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.io.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.utils.FileFormatUtils;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(name = "file.process.type", havingValue = "s3")
public class S3Service {

    private final AmazonS3 s3client;

    public S3Service(AmazonS3 s3client) {
        this.s3client = s3client;
    }

    public File getLatestFile(String bucketName) {
        ListObjectsV2Result result = s3client.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        log.info("Found {} objects in {}", objects.size(), bucketName);
        return objects.stream()
                .max(Comparator.comparing(S3ObjectSummary::getLastModified))
                .map(o -> {
                    try {
                        File file = new File(FileFormatUtils.createTimestampFile(o.getKey(), o.getLastModified()));
                        var s3Object = s3client.getObject(bucketName, o.getKey());
                        FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(s3Object.getObjectContent()));
                        return file;
                    } catch (IOException e) {
                        return null;
                    }
                }).orElse(null);
    }


}

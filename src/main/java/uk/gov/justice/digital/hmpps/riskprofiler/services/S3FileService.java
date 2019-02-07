package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(name = "file.process.type", havingValue = "s3")
public class S3FileService implements FileService {

    private final AmazonS3 s3client;

    public S3FileService(AmazonS3 s3client) {
        this.s3client = s3client;
    }

    public PendingFile getLatestFile(String bucketName) {
        ListObjectsV2Result result = s3client.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        log.info("Found {} objects in {}", objects.size(), bucketName);
        return objects.stream()
                .max(Comparator.comparing(S3ObjectSummary::getLastModified))
                .map(o -> {
                    try {
                        var s3Object = s3client.getObject(bucketName, o.getKey());
                        return PendingFile.builder()
                                .fileName(o.getKey())
                                .fileTimestamp(o.getLastModified().toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime())
                                .data(IOUtils.toByteArray(s3Object.getObjectContent()))
                                .build();

                    } catch (IOException e) {
                        return null;
                    }
                }).orElse(null);
    }


}

package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@ConditionalOnProperty(name = "file.process.type", havingValue = "s3")
public class S3FileService implements FileService {

    private Map<String, AmazonS3> bucketAccountMap;

    public S3FileService(@Qualifier("s3Client") AmazonS3 s3Client, @Qualifier("viperS3Client") AmazonS3 viperS3client,
                         @Value("${bucket.account.map}") List<String> clientList) {

        bucketAccountMap = getBucketClientMap(clientList).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().equals("s3Client") ? s3Client : viperS3client));

    }

    private Map<String, String> getBucketClientMap(@Value("${bucket.account.map}") List<String> clientList) {
        return clientList.stream()
                .collect(Collectors.toMap(
                        v -> StringUtils.split(v, "|")[0],
                        v -> StringUtils.split(v, "|")[1],
                        (v1, v2) -> {
                            log.warn("duplicate key found {}", v1);
                            return v1;
                        }));
    }


    public PendingFile getLatestFile(String fileLocation) {

        var s3Result = getObjectSummaries(fileLocation);

        log.info("Found {} objects in {}", s3Result.getObjects().size(), fileLocation);
        return s3Result.getObjects().stream()
                .max(Comparator.comparing(S3ObjectSummary::getLastModified))
                .map(o -> {
                    try {
                        var s3Object = s3Result.getAmazonS3Client().getObject(s3Result.getBucketName(), o.getKey());
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

    @Override
    public void deleteHistoricalFiles(String fileLocation) {
       var s3ObjectResult = getObjectSummaries(fileLocation);

        log.info("Found {} data files for data housekeeping in {}", s3ObjectResult.getObjects().size(), fileLocation);

        s3ObjectResult.getObjects().stream().sorted(Comparator.comparing(S3ObjectSummary::getLastModified).reversed()).skip(2).forEach(o -> {
            s3ObjectResult.getAmazonS3Client().deleteObject(s3ObjectResult.getBucketName(), o.getKey());
            log.info("Deleted s3 data file: {} from bucket {}", o.getKey(), s3ObjectResult.getBucketName());
        });
    }

    private ObjectSummaryResult getObjectSummaries(String fileLocation){
        var bucketAndPrefix = new BucketAndPrefix(fileLocation);
        var bucketName = bucketAndPrefix.getBucketName();
        var prefix = bucketAndPrefix.getPrefix();
        var amazonS3Client = bucketAccountMap.get(bucketName);

        ListObjectsV2Result result = amazonS3Client.listObjectsV2(bucketName, prefix);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        return new ObjectSummaryResult(objects, amazonS3Client, bucketName);
    }

    @Getter
    private static class BucketAndPrefix {
        private String bucketName;
        private String prefix;

        BucketAndPrefix(final String fileLocation) {
            var split = StringUtils.split(fileLocation, "/");
            bucketName = split[0];
            prefix = split.length > 1 ? split[1] : null;
        }
    }

    @Getter
    private class ObjectSummaryResult {
        private AmazonS3 amazonS3Client;
        private String bucketName;
        List<S3ObjectSummary> objects;

        ObjectSummaryResult(List<S3ObjectSummary> objects, AmazonS3 amazonS3Client, String bucketName) {
            this.objects = objects;
            this.amazonS3Client = amazonS3Client;
            this.bucketName = bucketName;
        }
    }
}

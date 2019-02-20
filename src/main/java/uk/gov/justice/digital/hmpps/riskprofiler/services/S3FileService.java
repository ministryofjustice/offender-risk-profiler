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

    public S3FileService(@Qualifier("s3client") AmazonS3 s3client, @Qualifier("viperS3Client") AmazonS3 viperS3client,
                         @Value("${bucket.account.map}") List<String> clientList) {

        bucketAccountMap = getBucketClientMap(clientList).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().equals("s3client") ? s3client : viperS3client));

    }

    private Map<String, String> getBucketClientMap(@Value("${bucket.account.map}") List<String> clientList) {
        return clientList.stream()
                .collect(Collectors.toMap(v -> StringUtils.split(v, "|")[0], v -> StringUtils.split(v, "|")[1]));
    }


    public PendingFile getLatestFile(String fileLocation) {
        var bucketAndPrefix = new BucketAndPrefix(fileLocation);
        var bucketName = bucketAndPrefix.getBucketName();
        var prefix = bucketAndPrefix.getPrefix();
        var amazonS3Client = bucketAccountMap.get(bucketName);

        ListObjectsV2Result result = amazonS3Client.listObjectsV2(bucketName, prefix);
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        log.info("Found {} objects in {}", objects.size(), fileLocation);
        return objects.stream()
                .max(Comparator.comparing(S3ObjectSummary::getLastModified))
                .map(o -> {
                    try {
                        var s3Object = amazonS3Client.getObject(bucketName, o.getKey());
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
}

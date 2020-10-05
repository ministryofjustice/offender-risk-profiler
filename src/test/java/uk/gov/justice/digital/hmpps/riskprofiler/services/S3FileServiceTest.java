package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class S3FileServiceTest {

    private static final String OFFENDER_1 = "AB1234A";
    private S3FileService service;

    @Mock
    private AmazonS3Client amazonS3Client;

    @Mock
    private AmazonS3Client amazonS3ViperClient;

    @Mock
    private ListObjectsV2Result result;

    @Before
    public void setup() {
        service = new S3FileService(amazonS3Client, amazonS3ViperClient, ImmutableList.of("risk-profiler|s3Client", "viper-risk-profiler|viperS3Client"));
    }

    @Test
    public void testDeleteHistoricalFiles() {


        final S3ObjectSummary s3ObjectSummary1 = new S3ObjectSummary();
        s3ObjectSummary1.setLastModified(java.sql.Timestamp.valueOf(LocalDateTime.of(2019, 10, 1, 1, 1)));
        s3ObjectSummary1.setKey("s3Ob1");
        final S3ObjectSummary s3ObjectSummary2 = new S3ObjectSummary();
        s3ObjectSummary2.setLastModified(java.sql.Timestamp.valueOf(LocalDateTime.of(2019, 10, 2, 2, 2)));
        s3ObjectSummary2.setKey("s3Ob2");
        final S3ObjectSummary s3ObjectSummary3 = new S3ObjectSummary();
        s3ObjectSummary3.setLastModified(java.sql.Timestamp.valueOf(LocalDateTime.of(2019, 10, 3, 3, 3)));
        s3ObjectSummary3.setKey("s3Ob3");
        final S3ObjectSummary s3ObjectSummary4 = new S3ObjectSummary();
        s3ObjectSummary4.setLastModified(java.sql.Timestamp.valueOf(LocalDateTime.of(2019, 10, 4, 4, 4)));
        s3ObjectSummary4.setKey("s3Ob4");

        when(amazonS3Client.listObjectsV2("risk-profiler", "gg")).thenReturn(result);
        when(result.getObjectSummaries()).thenReturn(ImmutableList.of(s3ObjectSummary1, s3ObjectSummary3, s3ObjectSummary4, s3ObjectSummary2));

        service.deleteHistoricalFiles("risk-profiler/gg");

        verify(amazonS3Client).deleteObject("risk-profiler", "s3Ob1");
        verify(amazonS3Client).deleteObject("risk-profiler", "s3Ob2");
        verify(amazonS3Client, never()).deleteObject("risk-profiler", "s3Ob3");
        verify(amazonS3Client, never()).deleteObject("risk-profiler", "s3Ob4");
    }

}

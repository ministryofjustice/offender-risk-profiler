package uk.gov.justice.digital.hmpps.riskprofiler.services

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ListObjectsV2Result
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.google.common.collect.ImmutableList
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.sql.Timestamp
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class S3FileServiceTest {
  private lateinit var service: S3FileService

  @Mock
  private lateinit var amazonS3Client: AmazonS3Client

  @Mock
  private lateinit var amazonS3ViperClient: AmazonS3Client

  @Mock
  private lateinit var result: ListObjectsV2Result

  @Before
  fun setup() {
    service = S3FileService(
      amazonS3Client
    )
  }

  @Test
  fun testDeleteHistoricalFiles() {
    val s3ObjectSummary1 = S3ObjectSummary()
    s3ObjectSummary1.lastModified = Timestamp.valueOf(LocalDateTime.of(2019, 10, 1, 1, 1))
    s3ObjectSummary1.key = "s3Ob1"
    val s3ObjectSummary2 = S3ObjectSummary()
    s3ObjectSummary2.lastModified = Timestamp.valueOf(LocalDateTime.of(2019, 10, 2, 2, 2))
    s3ObjectSummary2.key = "s3Ob2"
    val s3ObjectSummary3 = S3ObjectSummary()
    s3ObjectSummary3.lastModified = Timestamp.valueOf(LocalDateTime.of(2019, 10, 3, 3, 3))
    s3ObjectSummary3.key = "s3Ob3"
    val s3ObjectSummary4 = S3ObjectSummary()
    s3ObjectSummary4.lastModified = Timestamp.valueOf(LocalDateTime.of(2019, 10, 4, 4, 4))
    s3ObjectSummary4.key = "s3Ob4"
    Mockito.`when`(amazonS3Client.listObjectsV2("risk-profiler", "gg")).thenReturn(result)
    Mockito.`when`(result.objectSummaries)
      .thenReturn(ImmutableList.of(s3ObjectSummary1, s3ObjectSummary3, s3ObjectSummary4, s3ObjectSummary2))
    service.deleteHistoricalFiles("risk-profiler/gg")
    Mockito.verify(amazonS3Client).deleteObject("risk-profiler", "s3Ob1")
    Mockito.verify(amazonS3Client).deleteObject("risk-profiler", "s3Ob2")
    Mockito.verify(amazonS3Client, Mockito.never()).deleteObject("risk-profiler", "s3Ob3")
    Mockito.verify(amazonS3Client, Mockito.never()).deleteObject("risk-profiler", "s3Ob4")
  }
}

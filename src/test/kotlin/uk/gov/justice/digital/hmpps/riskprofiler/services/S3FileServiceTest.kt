package uk.gov.justice.digital.hmpps.riskprofiler.services

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ListObjectsV2Result
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.util.StringInputStream
import com.google.common.collect.ImmutableList
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import uk.gov.justice.digital.hmpps.riskprofiler.utils.readResourceAsText
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class S3FileServiceTest {
  private lateinit var service: S3FileService

  @Mock
  private lateinit var amazonS3Client: AmazonS3Client

  @Mock
  private lateinit var result: ListObjectsV2Result

  @Before
  fun setup() {
    service = S3FileService(
      amazonS3Client,
    )
  }

  @Test
  fun testProcessingViperFile() {
    val viperFile = loadTestData("VIPER_2_2024_10_29.csv")
    //   1209658,X0099AK,,0.283760332148618,0.52756476251039,-0.767545791412149,0.185067326935523,-0.483785459263531,0.55908361999052,2,FALSE
    //    0,A5015DY,,5.09880356645588e-05,1.09626459573684,-2.95133056520195,0.188235111383585,-2.95138155323762,1.1123077456458,3,FALSE
    //  """.trimIndent()

    amazonS3Client = mockS3Client("viper/VIPER_2_file.csv", viperFile)
    service = S3FileService(amazonS3Client)

    val pendingFile = service.getLatestFile("risk-profiler/viper/VIPER_2_file.csv", FileType.VIPER)
    assertThat(convertToString(pendingFile?.data!!)).isEqualTo(viperFile)
  }

  fun convertToString(inputStream: InputStream): String {
    return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name())
  }

  fun loadTestData(filename: String): String {
    val inputStream: InputStream = File("src/test/resources/testdata/$filename").inputStream()
    return inputStream.bufferedReader().use { it.readText() }
  }

  @Test
  fun testProcessingOcgmFile() {
    val ocgmFile = "classpath:localstack/buckets/ocgm/OCGM-Dummy.csv".readResourceAsText()

    amazonS3Client = mockS3Client("ocgm/file.csv", ocgmFile)
    service = S3FileService(amazonS3Client)

    val pendingFile = service.getLatestFile("risk-profiler/ocgm/file.csv", null)

    assertThat(convertToString(pendingFile?.data!!))
      .isEqualTo("classpath:localstack/buckets/ocgm/OCGM-Dummy.csv".readResourceAsText())
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

  private fun mockS3Client(key: String, fileToProcess: String): AmazonS3Client {
    amazonS3Client = Mockito.mock(AmazonS3Client::class.java)
    val listObjectsV2Result = Mockito.mock(ListObjectsV2Result::class.java)
    val s3ObjectSummary = S3ObjectSummary()
    s3ObjectSummary.lastModified = Timestamp.valueOf(LocalDateTime.of(2019, 10, 1, 1, 1))
    s3ObjectSummary.key = key

    Mockito.`when`(listObjectsV2Result.objectSummaries).thenReturn(ImmutableList.of(s3ObjectSummary))
    Mockito.`when`(amazonS3Client.listObjectsV2(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(listObjectsV2Result)

    val testInputStream: InputStream = StringInputStream(fileToProcess)
    val s3Object = S3Object()
    s3Object.setObjectContent(testInputStream)
    Mockito.`when`(amazonS3Client.getObject(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
      .thenReturn(s3Object)
    return amazonS3Client
  }
}

package uk.gov.justice.digital.hmpps.riskprofiler.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Response
import aws.sdk.kotlin.services.s3.model.Object
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Viper
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.time.ZoneId


@Component
@ConditionalOnProperty(name = ["file.process.type"], havingValue = "s3")
class S3FileService(
  @Qualifier("s3Client") private val s3Client: S3Client?,
) : FileService {

  override fun getLatestFile(fileLocation: String, fileType: FileType?): PendingFile? {
    /*
    val s3Result = getObjectSummaries(fileLocation)
    log.info("Found {} objects in {}", s3Result.objects.size, fileLocation)
    return s3Result.objects.stream()
      .max(Comparator.comparing { t -> t!!.lastModified })
      .map { o ->
        try {
          val s3Object = s3Result.amazonS3Client!!.getObject(s3Result.bucketName, o!!.key)
          return@map PendingFile(
            o.key,
            o.lastModified.toInstant()
              .atZone(ZoneId.systemDefault())
              .toLocalDateTime(),
            if (fileType == FileType.VIPER) getViperFile(s3Object.objectContent) else IOUtils.toByteArray(
              s3Object.objectContent,
            ),
          )
        } catch (e: IOException) {
          return@map null
        }
      }.orElse(null)

     */

    return null
  }

  override fun deleteHistoricalFiles(fileLocation: String) {
 /*   val s3ObjectResult = getObjectSummaries(fileLocation)
    log.info(
      "Found {} data files for data housekeeping in {}",
      s3ObjectResult.objects.size,
      fileLocation,
    )
    s3ObjectResult.objects.stream().sorted(
      Comparator.comparing { obj: Object -> obj.lastModified}
        .reversed(),
    ).skip(2).forEach { o ->
      s3ObjectResult.amazonS3Client!!.deleteObject(s3ObjectResult.bucketName, o!!.key)
      log.info("Deleted s3 data file: {} from bucket {}", o.key, s3ObjectResult.bucketName)
    }

  */
  }

  private fun getViperFile(s3ObjectContent: Object): ByteArray? {
    /*
    val reader = BufferedReader(InputStreamReader(s3ObjectContent))
    var row: List<String>
    val rowList = mutableListOf<String>()

    while (true) {
      val line = reader.readLine() ?: break
      row = line.split(",")
      if (row[Viper.RECORD_ID] != "0") {
        rowList.add(line)
      }
    }
    s3ObjectContent.close()

    val csv = rowList.joinToString(System.lineSeparator())
    return csv.toByteArray()

     */

    return null
  }

  private suspend fun getObjectSummaries(fileLocation: String): ObjectSummaryResult? {
    val bucketAndPrefix = BucketAndPrefix(fileLocation)
    val bucketName = bucketAndPrefix.bucketName!!
    val prefix = bucketAndPrefix.prefix
/*
    val listObjectsV2Request: ListObjectsV2Request = ListObjectsV2Request.builder
      .bucket(bucketName)
      .build()

    val listObjectsV2Response: ListObjectsV2Response = s3Client!!.listObjectsV2(listObjectsV2Request)

    for (os in listObjectsV2Response.contents!!) {
      System.out.println(os.key)
    }

    val objects = listObjectsV2Response.contents
    return ObjectSummaryResult(objects, s3Client, bucketName)

 */

    return null
  }

  private data class BucketAndPrefix(
    var bucketName: String?,
    var prefix: String?,
  ) {

    constructor(fileLocation: String) : this(null, null) {

      val split = StringUtils.split(fileLocation, "/")
      bucketName = split[0]
      prefix = if (split.size > 1) split[1] else null
    }
  }

  private data class ObjectSummaryResult(
    var objects: List<Object>?,
    val amazonS3Client: S3Client?,
    val bucketName: String,
  )

  companion object {
    private val log = LoggerFactory.getLogger(S3FileService::class.java)
  }
}

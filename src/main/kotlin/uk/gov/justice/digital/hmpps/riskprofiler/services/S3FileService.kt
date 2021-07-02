package uk.gov.justice.digital.hmpps.riskprofiler.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.util.IOUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.ZoneId
import java.util.stream.Collectors

// import java.util.Map

@Component
@ConditionalOnProperty(name = ["file.process.type"], havingValue = "s3")
class S3FileService(
  @Qualifier("s3Client") s3Client: AmazonS3?,
  @Qualifier("viperS3Client") viperS3client: AmazonS3?,
  @Value("\${bucket.account.map}") clientList: List<String>
) : FileService {
  private val bucketAccountMap: Map<String, AmazonS3>
  private fun getBucketClientMap(@Value("\${bucket.account.map}") clientList: List<String>): Map<String, String> {
    return clientList.stream()
      .collect(
        Collectors.toMap(
          { v: String? -> StringUtils.split(v, "|")[0] },
          { v: String? -> StringUtils.split(v, "|")[1] },
          { v1: String, v2: String? ->
            log.warn("duplicate key found {}", v1)
            v1
          }
        )
      )
  }

  override fun getLatestFile(fileLocation: String): PendingFile? {
    val s3Result = getObjectSummaries(fileLocation)
    log.info("Found {} objects in {}", s3Result.objects.size, fileLocation)
    return s3Result.objects.stream()
      .max(Comparator.comparing { t -> t!!.getLastModified() })
      .map { o ->
        try {
          val s3Object = s3Result.amazonS3Client!!.getObject(s3Result.bucketName, o!!.getKey())
          return@map PendingFile(
            o.getKey(),
            o.getLastModified().toInstant()
              .atZone(ZoneId.systemDefault())
              .toLocalDateTime(),
            IOUtils.toByteArray(s3Object.getObjectContent())
          )
        } catch (e: IOException) {
          return@map null
        }
      }.orElse(null)
  }

  override fun deleteHistoricalFiles(fileLocation: String) {
    val s3ObjectResult = getObjectSummaries(fileLocation)
    log.info("Found {} data files for data housekeeping in {}", s3ObjectResult.objects.size, fileLocation)
    s3ObjectResult.objects.stream().sorted(
      Comparator.comparing { obj: S3ObjectSummary -> obj.lastModified }
        .reversed()
    ).skip(2).forEach { o ->
      s3ObjectResult.amazonS3Client!!.deleteObject(s3ObjectResult.bucketName, o!!.getKey())
      log.info("Deleted s3 data file: {} from bucket {}", o!!.getKey(), s3ObjectResult.bucketName)
    }
  }

  private fun getObjectSummaries(fileLocation: String): ObjectSummaryResult {
    val bucketAndPrefix = BucketAndPrefix(fileLocation)
    val bucketName = bucketAndPrefix.bucketName!!
    val prefix = bucketAndPrefix.prefix
    val amazonS3Client = bucketAccountMap.get(bucketName)
    val result = amazonS3Client!!.listObjectsV2(bucketName, prefix)
    val objects = result.objectSummaries
    return ObjectSummaryResult(objects, amazonS3Client, bucketName)
  }

  private data class BucketAndPrefix(
    var bucketName: String?,
    var prefix: String?
  ) {
    internal constructor(fileLocation: String) : this(null, null) {

      val split = StringUtils.split(fileLocation, "/")
      bucketName = split[0]
      prefix = if (split.size > 1) split[1] else null
    }
  }

  private data class ObjectSummaryResult(
    var objects: List<S3ObjectSummary?>,
    val amazonS3Client: AmazonS3?,
    val bucketName: String
  )

  companion object {
    private val log = LoggerFactory.getLogger(S3FileService::class.java)
  }

  init {
    bucketAccountMap = getBucketClientMap(clientList).entries.stream()
      .collect(
        Collectors.toMap(
          Map.Entry<String, String>::key,
          { (key, value) -> if (value == "s3Client") s3Client else viperS3client }
        )
      ) as Map<String, AmazonS3>
  }
}

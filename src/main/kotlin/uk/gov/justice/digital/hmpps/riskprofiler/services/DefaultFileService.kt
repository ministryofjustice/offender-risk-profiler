package uk.gov.justice.digital.hmpps.riskprofiler.services

import com.amazonaws.util.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Arrays

@Component
@ConditionalOnProperty(name = ["file.process.type"], havingValue = "file")
class DefaultFileService : FileService {
  override fun getLatestFile(fileLocation: String): PendingFile? {
    val folder = File(fileLocation)
    val listOfFiles = folder.listFiles()
    if (listOfFiles != null) {
      log.info("Found {} files in {}", listOfFiles.size, fileLocation)
      return Arrays.stream(listOfFiles)
        .filter { f: File -> !f.isDirectory }
        .max(Comparator.comparing { obj: File -> obj.lastModified() })
        .map { f: File ->
          try {
            return@map PendingFile(
              f.name,
              LocalDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), ZoneId.systemDefault()),
              IOUtils.toByteArray(FileInputStream(f))
            )
          } catch (e: IOException) {
            return@map null
          }
        }
        .orElse(null)
    }
    log.info("Found {} files in {}", 0, fileLocation)
    return null
  }

  override fun deleteHistoricalFiles(fileLocation: String) {
    val folder = File(fileLocation)
    val listOfFiles = folder.listFiles()
    if (listOfFiles != null) {
      log.info("Housekeeping- found {} files in {}", listOfFiles.size, fileLocation)
      Arrays.stream(listOfFiles).sorted(Comparator.comparing { obj: File -> obj.lastModified() }
        .reversed()).skip(2).forEach { file: File ->
        file.delete()
        log.info("Deleted file {} ", fileLocation + "/" + file.name)
      }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(DefaultFileService::class.java)
  }
}

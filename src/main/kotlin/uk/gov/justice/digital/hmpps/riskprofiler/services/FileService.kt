package uk.gov.justice.digital.hmpps.riskprofiler.services

interface FileService {
  fun getLatestFile(fileLocation: String): PendingFile?
  fun deleteHistoricalFiles(fileLocation: String)
}

package uk.gov.justice.digital.hmpps.riskprofiler.services

import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType

interface FileService {
  fun getLatestFile(fileLocation: String, fileType: FileType?): PendingFile?
  fun deleteHistoricalFiles(fileLocation: String)
}

package uk.gov.justice.digital.hmpps.riskprofiler.utils

import org.apache.commons.io.FileUtils
import org.springframework.util.ResourceUtils
import java.nio.charset.StandardCharsets

fun String.readResourceAsText(): String {
  return FileUtils.readFileToString(ResourceUtils.getFile(this), StandardCharsets.UTF_8)
}

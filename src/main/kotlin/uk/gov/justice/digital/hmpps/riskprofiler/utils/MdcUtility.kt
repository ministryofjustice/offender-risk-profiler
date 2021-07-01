package uk.gov.justice.digital.hmpps.riskprofiler.utils

import org.slf4j.MDC
import org.springframework.stereotype.Component

@Component
object MdcUtility {
  const val USER_ID_HEADER = "userId"
  const val REQUEST_DURATION = "duration"
  const val RESPONSE_STATUS = "status"
  const val SKIP_LOGGING = "skipLogging"
  val isLoggingAllowed: Boolean
    get() = "true" != MDC.get(SKIP_LOGGING)
}

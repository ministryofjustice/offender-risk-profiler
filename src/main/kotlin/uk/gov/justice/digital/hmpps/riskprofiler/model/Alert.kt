package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.time.LocalDate

@RedisHash("Alert")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Alert(

  @Id private val alertId: Long? = null,
  private val bookingId: Long? = null,
  private val offenderNo: String? = null,
  private val alertType: String? = null,
  private val alertTypeDescription: String? = null,
  val alertCode: String,
  private val alertCodeDescription: String? = null,
  private val comment: String? = null,
  var dateCreated: LocalDate,
  var dateExpires: LocalDate? = null,
  var expired: Boolean,
  val active: Boolean,
  private val addedByFirstName: String? = null,
  private val addedByLastName: String? = null,
  private val expiredByFirstName: String? = null,
  private val expiredByLastName: String? = null,
  private val ranking: Int? = 0,
) : Serializable {

  /** for tests */
  constructor(active: Boolean, expired: Boolean, code: String) : this(
    active = active,
    expired = expired,
    alertCode = code,
    dateCreated = LocalDate.now()
  )

  companion object {
    private const val serialVersionUID: Long = 1
  }
}

package uk.gov.justice.digital.hmpps.riskprofiler.factories.model

import uk.gov.justice.digital.hmpps.riskprofiler.dto.prisonerAlert.PrisonerAlertCodeSummaryDto
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert
import java.time.LocalDate

class TestAlertFactory {

  private var alertCode: String = PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_RISK
  private var dateCreated: LocalDate = LocalDate.of(2025, 8, 1)
  private var activeFrom: LocalDate = LocalDate.of(2024, 5, 1)
  private var dateExpires: LocalDate? = LocalDate.of(2025, 8, 1)
  private var active: Boolean = true

  fun withAlertCode(alertCode: String): TestAlertFactory {
    this.alertCode = alertCode
    return this
  }
  fun withDateCreated(dateCreated: LocalDate): TestAlertFactory {
    this.dateCreated = dateCreated
    return this
  }
  fun withActiveFrom(activeFrom: LocalDate): TestAlertFactory {
    this.activeFrom = activeFrom
    return this
  }
  fun withDateExpires(dateExpires: LocalDate?): TestAlertFactory {
    this.dateExpires = dateExpires
    return this
  }
  fun withActive(active: Boolean): TestAlertFactory {
    this.active = active
    return this
  }

  fun build(): Alert {
    return Alert(
      this.alertCode,
      this.dateCreated,
      this.activeFrom,
      this.dateExpires,
      this.active
    )
  }
}
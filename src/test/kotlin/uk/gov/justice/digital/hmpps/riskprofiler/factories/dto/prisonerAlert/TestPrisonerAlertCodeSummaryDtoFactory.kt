package uk.gov.justice.digital.hmpps.riskprofiler.factories.dto.prisonerAlert

import uk.gov.justice.digital.hmpps.riskprofiler.dto.prisonerAlert.PrisonerAlertCodeSummaryDto

class TestPrisonerAlertCodeSummaryDtoFactory {

  private var alertCode = PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_RISK
  private var alertDescription = "Test alert description"

  fun withAlertCode(alertCode: String): TestPrisonerAlertCodeSummaryDtoFactory {
    this.alertCode = alertCode
    return this
  }

  fun withAlertDescription(alertDescription: String): TestPrisonerAlertCodeSummaryDtoFactory {
    this.alertDescription = alertDescription
    return this
  }

  fun build(): PrisonerAlertCodeSummaryDto {
    return PrisonerAlertCodeSummaryDto(
      this.alertCode,
      this.alertDescription
    )
  }
}
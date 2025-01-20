package uk.gov.justice.digital.hmpps.riskprofiler.factories.dto.prisonerAlert

import uk.gov.justice.digital.hmpps.riskprofiler.dto.prisonerAlert.PrisonerAlertCodeSummaryDto
import uk.gov.justice.digital.hmpps.riskprofiler.dto.prisonerAlert.PrisonerAlertResponseDto
import java.time.LocalDate

class TestPrisonerAlertResponseDtoFactory {
  private var alertCodeSummary: PrisonerAlertCodeSummaryDto = (TestPrisonerAlertCodeSummaryDtoFactory()).build()
  private var createdAt: LocalDate = LocalDate.of(2024, 4, 1)
  private var activeTo: LocalDate? = null
  private var activeFrom: LocalDate = LocalDate.of(2024, 5, 1)
  private var active: Boolean = true

  fun withAlertCodeSummary(alertCodeSummary: PrisonerAlertCodeSummaryDto): TestPrisonerAlertResponseDtoFactory {
    this.alertCodeSummary = alertCodeSummary
    return this
  }
  fun withCreatedAt(createdAt: LocalDate): TestPrisonerAlertResponseDtoFactory {
    this.createdAt = createdAt
    return this
  }
  fun withActiveTo(activeTo: LocalDate?): TestPrisonerAlertResponseDtoFactory {
    this.activeTo = activeTo
    return this
  }
  fun withActiveFrom(activeFrom: LocalDate): TestPrisonerAlertResponseDtoFactory {
    this.activeFrom = activeFrom
    return this
  }
  fun withActive(active: Boolean): TestPrisonerAlertResponseDtoFactory {
    this.active = active
    return this
  }

  fun build(): PrisonerAlertResponseDto {
    return PrisonerAlertResponseDto(
      this.alertCodeSummary,
      this.createdAt,
      this.activeTo,
      this.activeFrom,
      this.active
    )
  }
}
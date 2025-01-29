package uk.gov.justice.digital.hmpps.riskprofiler.mappers.prisonerAlert

import uk.gov.justice.digital.hmpps.riskprofiler.dto.prisonerAlert.PrisonerAlertResponseDto
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert

class PrisonerAlertResponseDtoMapper {
  companion object {
    private fun mapToAlert(prisonerAlertResponseDto: PrisonerAlertResponseDto): Alert {
      return Alert(
        prisonerAlertResponseDto.alertCode.code,
        prisonerAlertResponseDto.createdAt,
        prisonerAlertResponseDto.activeFrom,
        prisonerAlertResponseDto.activeTo,
        prisonerAlertResponseDto.active
      )
    }

    fun mapAllToAlerts(prisonerAlertResponseDtos: List<PrisonerAlertResponseDto>): List<Alert> {
      return prisonerAlertResponseDtos.map { mapToAlert(it) }
    }
  }
}
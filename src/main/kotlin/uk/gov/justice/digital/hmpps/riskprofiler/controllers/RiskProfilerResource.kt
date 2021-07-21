package uk.gov.justice.digital.hmpps.riskprofiler.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import io.swagger.annotations.Authorization
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.riskprofiler.model.ErrorResponse
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.ExtremismProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.LifeProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.ViolenceProfile
import uk.gov.justice.digital.hmpps.riskprofiler.services.EscapeDecisionTreeService
import uk.gov.justice.digital.hmpps.riskprofiler.services.ExtremismDecisionTreeService
import uk.gov.justice.digital.hmpps.riskprofiler.services.LifeDecisionTreeService
import uk.gov.justice.digital.hmpps.riskprofiler.services.SocDecisionTreeService
import uk.gov.justice.digital.hmpps.riskprofiler.services.ViolenceDecisionTreeService
import javax.validation.constraints.NotNull

@Api(
  tags = ["risk-profile"],
  authorizations = [Authorization("RISK_PROFILER")],
  description = "Provides Offender Risk Profile Information on SOC, Escape, Violence, Extremism"
)
@RestController
@RequestMapping(value = ["risk-profile"], produces = [MediaType.APPLICATION_JSON_VALUE])
class RiskProfilerResource(
  private val socDecisionTreeServiceService: SocDecisionTreeService,
  private val violenceDecisionTreeService: ViolenceDecisionTreeService,
  private val escapeDecisionTreeService: EscapeDecisionTreeService,
  private val extremismDecisionTreeService: ExtremismDecisionTreeService,
  private val lifeDecisionTreeService: LifeDecisionTreeService
) {
  @ApiOperation(
    value = "Return SOC Risk for offender",
    notes = "Value can be true or false",
    authorizations = [Authorization("RISK_PROFILER")],
    nickname = "getSoc"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = SocProfile::class), ApiResponse(
        code = 400,
        message = "Invalid request",
        response = ErrorResponse::class
      ), ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @GetMapping(path = ["/soc/{nomsId}"])
  fun getSoc(
    @ApiParam(
      name = "nomsId",
      value = "NOMS ID of the offender",
      example = "A1234AA",
      required = true
    ) @PathVariable(value = "nomsId") nomsId: @NotNull String?
  ): SocProfile {
    return socDecisionTreeServiceService.getSocData(nomsId)
  }

  @ApiOperation(
    value = "Return Escape Risk for offender",
    notes = "Value can be true or false",
    authorizations = [Authorization("RISK_PROFILER")],
    nickname = "getEscape"
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 200,
        message = "OK",
        response = EscapeProfile::class
      ), ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse::class), ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @GetMapping(path = ["/escape/{nomsId}"])
  fun getEscape(
    @ApiParam(
      name = "nomsId",
      value = "NOMS ID of the offender",
      example = "A1234AA",
      required = true
    ) @PathVariable("nomsId") nomsId: @NotNull String?
  ): EscapeProfile {
    return escapeDecisionTreeService.getEscapeProfile(nomsId)
  }

  @ApiOperation(
    value = "Return Violence Risk for offender",
    notes = "Value can be high, low or none",
    authorizations = [Authorization("RISK_PROFILER")],
    nickname = "getViolence"
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 200,
        message = "OK",
        response = ViolenceProfile::class
      ), ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse::class), ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @GetMapping(path = ["/violence/{nomsId}"])
  fun getViolence(
    @ApiParam(
      name = "nomsId",
      value = "NOMS ID of the offender",
      example = "A1234AA",
      required = true
    ) @PathVariable("nomsId") nomsId: @NotNull String?
  ): ViolenceProfile {
    return violenceDecisionTreeService.getViolenceProfile(nomsId)
  }

  @ApiOperation(
    value = "Return Extremism Risk for offender",
    notes = "Value can be true or false",
    authorizations = [Authorization("RISK_PROFILER")],
    nickname = "getExtremism"
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 200,
        message = "OK",
        response = ExtremismProfile::class
      ), ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse::class), ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @GetMapping(path = ["/extremism/{nomsId}"])
  fun getExtremism(
    @ApiParam(
      name = "nomsId",
      value = "NOMS ID of the offender",
      example = "A1234AA",
      required = true
    ) @PathVariable("nomsId") nomsId: @NotNull String?,
    @ApiParam(
      name = "previousOffences",
      value = "Previous Offences under Terrorism Act listed on the person's PNC record",
      required = false,
      example = "false"
    ) @RequestParam(value = "previousOffences", required = false) previousOffences: Boolean?
  ): ExtremismProfile {
    return extremismDecisionTreeService.getExtremismProfile(nomsId!!, previousOffences)
  }

  @ApiOperation(
    value = "Assess whether offender has a life sentence",
    authorizations = [Authorization("RISK_PROFILER")]
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = LifeProfile::class), ApiResponse(
        code = 400,
        message = "Invalid request",
        response = ErrorResponse::class
      ), ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @GetMapping(path = ["/life/{nomsId}"])
  fun getLife(
    @ApiParam(
      name = "nomsId",
      value = "NOMS ID of the offender",
      example = "A1234AA",
      required = true
    ) @PathVariable("nomsId") nomsId: @NotNull String?
  ): LifeProfile {
    return lifeDecisionTreeService.getLifeProfile(nomsId)
  }
}

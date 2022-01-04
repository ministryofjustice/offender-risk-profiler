package uk.gov.justice.digital.hmpps.riskprofiler.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(
  name = "risk-profile",
  description = "Provides Offender Risk Profile Information on SOC, Escape, Violence, Extremism"
)
@ApiResponses(
  value = [
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
    ),
  ]
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
  @Operation(
    summary = "Return SOC Risk for offender",
    description = "Value can be true or false",
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @GetMapping(path = ["/soc/{nomsId}"])
  fun getSoc(
    @Parameter(
      name = "nomsId",
      description = "NOMS ID of the offender",
      example = "A1234AA",
      required = true
    ) @PathVariable(value = "nomsId") nomsId: String
  ): SocProfile {
    return socDecisionTreeServiceService.getSocData(nomsId)
  }

  @Operation(
    summary = "Return Escape Risk for offender",
    description = "Value can be true or false",
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @GetMapping(path = ["/escape/{nomsId}"])
  fun getEscape(
    @Parameter(
      name = "nomsId",
      description = "NOMS ID of the offender",
      example = "A1234AA",
      required = true
    ) @PathVariable("nomsId") nomsId: String
  ): EscapeProfile {
    return escapeDecisionTreeService.getEscapeProfile(nomsId)
  }

  @Operation(
    summary = "Return Violence Risk for offender",
    description = "Value can be high, low or none",
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @GetMapping(path = ["/violence/{nomsId}"])
  fun getViolence(
    @Parameter(
      name = "nomsId",
      description = "NOMS ID of the offender",
      example = "A1234AA",
      required = true
    ) @PathVariable("nomsId") nomsId: String
  ): ViolenceProfile {
    return violenceDecisionTreeService.getViolenceProfile(nomsId)
  }

  @Operation(
    summary = "Return Extremism Risk for offender",
    description = "Value can be true or false",
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @GetMapping(path = ["/extremism/{nomsId}"])
  fun getExtremism(
    @Parameter(
      name = "nomsId",
      description = "NOMS ID of the offender",
      example = "A1234AA",
      required = true
    ) @PathVariable("nomsId") nomsId: String,
    @Parameter(
      name = "previousOffences",
      description = "Previous Offences under Terrorism Act listed on the person's PNC record",
      required = false,
      example = "false"
    ) @RequestParam(value = "previousOffences", required = false) previousOffences: Boolean?
  ): ExtremismProfile {
    return extremismDecisionTreeService.getExtremismProfile(nomsId, previousOffences)
  }

  @Operation(
    summary = "Assess whether offender has a life sentence",
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @GetMapping(path = ["/life/{nomsId}"])
  fun getLife(
    @Parameter(
      name = "nomsId",
      description = "NOMS ID of the offender",
      example = "A1234AA",
      required = true
    ) @PathVariable("nomsId") nomsId: String
  ): LifeProfile {
    return lifeDecisionTreeService.getLifeProfile(nomsId)
  }
}

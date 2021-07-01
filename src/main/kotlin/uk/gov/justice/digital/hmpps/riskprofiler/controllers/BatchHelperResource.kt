package uk.gov.justice.digital.hmpps.riskprofiler.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import io.swagger.annotations.Authorization
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.riskprofiler.events.QueueAdminService
import uk.gov.justice.digital.hmpps.riskprofiler.model.ErrorResponse
import uk.gov.justice.digital.hmpps.riskprofiler.schedule.PollPrisonersScheduler
import uk.gov.justice.digital.hmpps.riskprofiler.services.PrisonService
import javax.validation.constraints.NotNull

@Api(
  tags = ["batch"],
  authorizations = [Authorization("RISK_PROFILER")],
  description = "Provides ability to configure and run batch"
)
@RestController
@RequestMapping(value = ["batch-helper"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BatchHelperResource(
  private val pollPrisonersScheduler: PollPrisonersScheduler,
  private val prisonService: PrisonService,
  private val queueAdminService: QueueAdminService
) {
  @ApiOperation(value = "Start a batch job run", authorizations = [Authorization("RISK_PROFILER")])
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK"), ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @PostMapping(path = ["/startPollPrisoners"])
  fun startPollPrisoners() {
    pollPrisonersScheduler.pollPrisoners()
    // could use camel if we want async kickoff:
    // producerTemplate.send("direct:poll-prisoners", exchange -> {});
  }

  @ApiOperation(
    value = "Add prison to the uk.gov.justice.digital.hmpps.config",
    authorizations = [Authorization("RISK_PROFILER")]
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK"), ApiResponse(
        code = 400,
        message = "Invalid prison id",
        response = ErrorResponse::class
      ), ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @PostMapping(path = ["/prison/{prisonId}"])
  fun addPrison(
    @ApiParam(
      name = "prisonId",
      value = "Agency id of the prison",
      example = "LEI",
      required = true
    ) @PathVariable("prisonId") prisonId: @NotNull String
  ) =
    prisonService.addPrison(prisonId)

  @ApiOperation(
    value = "Trigger the transfer of any DLQ messages to the event queue",
    authorizations = [Authorization("RISK_PROFILER")]
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @PostMapping(path = ["/transferEventMessages"])
  fun transferEventMessages() {
    queueAdminService.transferEventMessages()
  }
}

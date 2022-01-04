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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.riskprofiler.events.QueueAdminService
import uk.gov.justice.digital.hmpps.riskprofiler.model.ErrorResponse
import uk.gov.justice.digital.hmpps.riskprofiler.schedule.PollPrisonersScheduler
import uk.gov.justice.digital.hmpps.riskprofiler.services.PrisonService
import javax.validation.constraints.NotNull

@Tag(
  name = "batch",
  description = "Provides ability to configure and run batches"
)
@ApiResponses(
  value = [
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid prison id.",
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
@RequestMapping(value = ["batch-helper"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BatchHelperResource(
  private val pollPrisonersScheduler: PollPrisonersScheduler,
  private val prisonService: PrisonService,
  private val queueAdminService: QueueAdminService
) {
  @Operation(summary = "Start a batch job run")
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @PostMapping(path = ["/startPollPrisoners"])
  fun startPollPrisoners() {
    pollPrisonersScheduler.pollPrisoners()
    // could use camel if we want async kickoff:
    // producerTemplate.send("direct:poll-prisoners", exchange -> {});
  }

  @Operation(
    summary = "Add prison to the config",
    description = "The overnight polling batch will then include this prison"
  )
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @PostMapping(path = ["/prison/{prisonId}"])
  fun addPrison(
    @Parameter(
      name = "prisonId",
      description = "Agency id of the prison",
      example = "LEI",
      required = true
    ) @PathVariable("prisonId") prisonId: @NotNull String
  ) =
    prisonService.addPrison(prisonId)

  @Operation(summary = "Trigger the transfer of any DLQ messages back to the event queue to be retried")
  @PreAuthorize("hasRole('RISK_PROFILER')")
  @PostMapping(path = ["/transferEventMessages"])
  fun transferEventMessages() =
    queueAdminService.transferEventMessages()
}

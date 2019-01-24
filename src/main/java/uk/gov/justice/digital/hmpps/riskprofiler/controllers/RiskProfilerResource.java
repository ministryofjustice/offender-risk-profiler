package uk.gov.justice.digital.hmpps.riskprofiler.controllers;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.hmpps.riskprofiler.model.*;
import uk.gov.justice.digital.hmpps.riskprofiler.services.EscapeDecisionTreeService;
import uk.gov.justice.digital.hmpps.riskprofiler.services.ExtremismDecisionTreeService;
import uk.gov.justice.digital.hmpps.riskprofiler.services.SocDecisionTreeService;
import uk.gov.justice.digital.hmpps.riskprofiler.services.ViolenceDecisionTreeService;

import javax.validation.constraints.NotNull;

@Api(tags = {"risk-profile"},
        authorizations = { @Authorization("RISK_PROFILER") },
        description = "Provides Offender Risk Profile Information on SOC, Escape, Violence, Extremism")

@RestController
@RequestMapping(
        value="risk-profile",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class RiskProfilerResource {

    private final SocDecisionTreeService socDecisionTreeServiceService;
    private final ViolenceDecisionTreeService violenceDecisionTreeService;
    private final EscapeDecisionTreeService escapeDecisionTreeService;
    private final ExtremismDecisionTreeService extremismDecisionTreeService;

    public RiskProfilerResource(SocDecisionTreeService socDecisionTreeServiceService, ViolenceDecisionTreeService violenceDecisionTreeService, EscapeDecisionTreeService escapeDecisionTreeService, ExtremismDecisionTreeService extremismDecisionTreeService) {
        this.socDecisionTreeServiceService = socDecisionTreeServiceService;
        this.violenceDecisionTreeService = violenceDecisionTreeService;
        this.escapeDecisionTreeService = escapeDecisionTreeService;
        this.extremismDecisionTreeService = extremismDecisionTreeService;
    }

    @ApiOperation(
            value = "Return SOC Risk for offender",
            notes = "Value can be true or false",
            authorizations = { @Authorization("RISK_PROFILER") },
            nickname="getSoc")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SocProfile.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    @GetMapping(path = "/soc/{nomsId}")
    public SocProfile getSoc(@ApiParam("nomsId") @NotNull @PathVariable("nomsId") String nomsId) {
        return socDecisionTreeServiceService.getSocData(nomsId);
    }

    @ApiOperation(
            value = "Return Escape Risk for offender",
            notes = "Value can be true or false",
            authorizations = { @Authorization("RISK_PROFILER") },
            nickname="getEscape")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = EscapeProfile.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    @GetMapping(path = "/escape/{nomsId}")
    public EscapeProfile getEscape(@ApiParam("nomsId") @NotNull @PathVariable("nomsId") String nomsId) {

        return escapeDecisionTreeService.getEscapeProfile(nomsId);
    }

    @ApiOperation(
            value = "Return Violence Risk for offender",
            notes = "Value can be high, low or none",
            authorizations = { @Authorization("RISK_PROFILER") },
            nickname="getViolence")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ViolenceProfile.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    @GetMapping(path = "/violence/{nomsId}")
    public ViolenceProfile getViolence(@ApiParam("nomsId") @NotNull @PathVariable("nomsId") String nomsId) {

        return violenceDecisionTreeService.getViolenceProfile(nomsId);

    }

    @ApiOperation(
            value = "Return Extremism Risk for offender",
            notes = "Value can be true or false",
            authorizations = { @Authorization("RISK_PROFILER") },
            nickname="getExtremism")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ExtremismProfile.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    @GetMapping(path = "/extremism/{nomsId}/{previousOffences}")
    public ExtremismProfile getExtremism(@ApiParam("nomsId") @NotNull @PathVariable("nomsId") String nomsId,
                                         @ApiParam("previousOffences") @NotNull @PathVariable("previousOffences") boolean previousOffences) {

        return extremismDecisionTreeService.getExtremismProfile(nomsId, previousOffences);

    }
}

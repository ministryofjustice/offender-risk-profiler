package uk.gov.justice.digital.hmpps.riskprofiler.controllers;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.hmpps.riskprofiler.dto.ErrorResponse;
import uk.gov.justice.digital.hmpps.riskprofiler.dto.RiskProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.dto.RiskType;

import javax.validation.constraints.NotNull;

@Api(tags = {"risk-profile"})

@RestController
@RequestMapping(
        value="risk-profile",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class RiskProfilerResource {

    @ApiOperation(
            value = "Return SOC Risk for offender",
            notes = "Value can be true or false",
            nickname="getSoc")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = RiskProfile.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    @GetMapping(path = "/soc/{nomsId}")
    public RiskProfile getSoc(@ApiParam("nomsId") @NotNull @PathVariable("nomsId") String nomsId) {

        return RiskProfile.builder()
                .nomsId(nomsId)
                .riskType(RiskType.SOC)
                .result("true")
                .build();

    }

    @ApiOperation(
            value = "Return Escape Risk for offender",
            notes = "Value can be true or false",
            nickname="getEscape")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = RiskProfile.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    @GetMapping(path = "/escape/{nomsId}")
    public RiskProfile getEscape(@ApiParam("nomsId") @NotNull @PathVariable("nomsId") String nomsId) {

        return RiskProfile.builder()
                .nomsId(nomsId)
                .riskType(RiskType.ESCAPE)
                .result("false")
                .build();

    }

    @ApiOperation(
            value = "Return Violence Risk for offender",
            notes = "Value can be high, low or none",
            nickname="getViolence")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = RiskProfile.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    @GetMapping(path = "/violence/{nomsId}")
    public RiskProfile getViolence(@ApiParam("nomsId") @NotNull @PathVariable("nomsId") String nomsId) {

        return RiskProfile.builder()
                .nomsId(nomsId)
                .riskType(RiskType.VIOLENCE)
                .result("HIGH")
                .build();

    }

    @ApiOperation(
            value = "Return Extremism Risk for offender",
            notes = "Value can be true or false",
            nickname="getExtremism")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = RiskProfile.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    @GetMapping(path = "/extremism/{nomsId}")
    public RiskProfile getExtremism(@ApiParam("nomsId") @NotNull @PathVariable("nomsId") String nomsId) {

        return RiskProfile.builder()
                .nomsId(nomsId)
                .riskType(RiskType.EXTREMISM)
                .result("true")
                .build();

    }
}

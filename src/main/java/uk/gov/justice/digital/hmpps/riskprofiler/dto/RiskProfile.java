package uk.gov.justice.digital.hmpps.riskprofiler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "RiskProfile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskProfile {
    @ApiModelProperty(required = true, value = "Identifies the offender by NOMS ID.", example = "ZWE123A", position = 0)
    @NotBlank
    private String nomsId;

    @ApiModelProperty(required = true, value = "Risk Type, VIOLENCE, SOC, EXTREMISM, ESCAPE;", example = "EXTREMISM", position = 1)
    @NotBlank
    private RiskType riskType;

    @ApiModelProperty(required = true, value = "Result of Risk", example = "true", position = 2)
    @NotBlank
    private String result;
}

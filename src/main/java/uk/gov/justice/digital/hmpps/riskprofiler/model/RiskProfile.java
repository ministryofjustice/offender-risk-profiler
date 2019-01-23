package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "RiskProfile")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@ToString
public abstract class RiskProfile {

    public final static String DEFAULT_CAT = "C";

    @ApiModelProperty(required = true, value = "Identifies the offender by NOMS ID.", example = "ZWE123A", position = 0)
    @NotBlank
    private String nomsId;

    @ApiModelProperty(required = true, value = "Risk Type, VIOLENCE, SOC, EXTREMISM, ESCAPE;", example = "EXTREMISM", position = 1)
    @NotBlank
    private RiskType riskType;

    @ApiModelProperty(required = true, value = "Provisional Categorisation", example = "C", position = 2)
    @NotBlank
    @Builder.Default
    private String provisionalCategorisation = "C";

}

package uk.gov.justice.digital.hmpps.riskprofiler.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class EscapeProfile extends RiskProfile {
    @ApiModelProperty(value = "Indicates On Heightened / Standard / Escort E-List", example = "false", position = 3)
    private boolean onEscapeList;

    @ApiModelProperty(value = "Notify Active On Escape List", example = "true", position = 4)
    private boolean activeOnEscapeList;

    public RiskType getRiskType() {
        return RiskType.ESCAPE;
    }

    @Builder(builderMethodName = "escapeBuilder")
    public EscapeProfile(@NotBlank String nomsId, @NotBlank String provisionalCategorisation, boolean onEscapeList, boolean activeOnEscapeList) {
        super(nomsId, provisionalCategorisation);
        this.onEscapeList = onEscapeList;
        this.activeOnEscapeList = activeOnEscapeList;
    }
}

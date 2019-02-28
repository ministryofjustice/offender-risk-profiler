package uk.gov.justice.digital.hmpps.riskprofiler.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class EscapeProfile extends RiskProfile {

    @ApiModelProperty(value = "Indicates offender is on the escape list", example = "true", position = 1)
    private boolean activeEscapeList;

    @ApiModelProperty(value = "Indicates offender is an escape risk", example = "true", position = 2)
    private boolean activeEscapeRisk;

    @ApiModelProperty(value = "Active escape risk alerts", position = 3)
    private List<Alert> escapeRiskAlerts;

    @ApiModelProperty(value = "Active escape list alerts", position = 4)
    private List<Alert> escapeListAlerts;

    public RiskType getRiskType() {
        return RiskType.ESCAPE;
    }

    @Builder(builderMethodName = "escapeBuilder")
    public EscapeProfile(@NotBlank String nomsId, @NotBlank String provisionalCategorisation, boolean activeEscapeList, boolean activeEscapeRisk, List<Alert> escapeListAlerts, List<Alert> escapeRiskAlerts) {
        super(nomsId, provisionalCategorisation);
        this.activeEscapeList = activeEscapeList;
        this.activeEscapeRisk = activeEscapeRisk;
        this.escapeListAlerts = escapeListAlerts;
        this.escapeRiskAlerts = escapeRiskAlerts;
    }
}

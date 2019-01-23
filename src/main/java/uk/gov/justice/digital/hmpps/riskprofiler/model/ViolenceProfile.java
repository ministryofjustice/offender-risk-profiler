package uk.gov.justice.digital.hmpps.riskprofiler.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class ViolenceProfile extends RiskProfile {
    @ApiModelProperty(value = "Indicates that offender is very high risk of violence", example = "false", position = 3)
    private boolean veryHighRiskViolentOffender;

    @ApiModelProperty(value = "Notify That Safety Custody Lead should be informed", example = "true", position = 4)
    private boolean notifySafetyCustodyLead;

    @ApiModelProperty(value = "Indicates that number of assults and number serious should be displayed", example = "false", position = 4)
    private boolean displayAssaults;

    @Builder(builderMethodName = "violenceBuilder")
    public ViolenceProfile(@NotBlank String nomsId, @NotBlank String provisionalCategorisation, boolean veryHighRiskViolentOffender, boolean notifySafetyCustodyLead, boolean displayAssaults) {
        super(nomsId, RiskType.VIOLENCE, provisionalCategorisation);
        this.veryHighRiskViolentOffender = veryHighRiskViolentOffender;
        this.notifySafetyCustodyLead = notifySafetyCustodyLead;
        this.displayAssaults = displayAssaults;
    }
}

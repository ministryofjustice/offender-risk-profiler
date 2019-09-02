package uk.gov.justice.digital.hmpps.riskprofiler.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ViolenceProfile extends RiskProfile {
    @ApiModelProperty(value = "Indicates that offender is very high risk of violence", example = "false", position = 3)
    private boolean veryHighRiskViolentOffender;

    @ApiModelProperty(value = "Notify That Safety Custody Lead should be informed", example = "true", position = 4)
    private boolean notifySafetyCustodyLead;

    @ApiModelProperty(value = "Indicates that number of assaults and number serious should be displayed", example = "false", position = 5)
    private boolean displayAssaults;

    @ApiModelProperty(value = "The number of assaults for this offender", example = "4", position = 6)
    private Long numberOfAssaults;

    @ApiModelProperty(value = "The number of serious assaults in the last 12 months", example = "2", position = 7)
    private Long numberOfSeriousAssaults;

    public RiskType getRiskType() {
        return RiskType.VIOLENCE;
    }

    @Builder(builderMethodName = "violenceBuilder")
    public ViolenceProfile(@NotBlank String nomsId, @NotBlank String provisionalCategorisation,
                           boolean veryHighRiskViolentOffender, boolean notifySafetyCustodyLead, boolean displayAssaults,
                           long numberOfAssaults, long numberOfSeriousAssaults) {
        super(nomsId, provisionalCategorisation);
        this.veryHighRiskViolentOffender = veryHighRiskViolentOffender;
        this.notifySafetyCustodyLead = notifySafetyCustodyLead;
        this.displayAssaults = displayAssaults;
        this.numberOfAssaults = numberOfAssaults;
        this.numberOfSeriousAssaults = numberOfSeriousAssaults;
    }

}

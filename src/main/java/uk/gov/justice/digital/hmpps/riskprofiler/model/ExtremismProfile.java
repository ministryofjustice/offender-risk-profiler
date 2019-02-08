package uk.gov.justice.digital.hmpps.riskprofiler.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class ExtremismProfile extends RiskProfile {
    @ApiModelProperty(value = "Indicates Regional CT Lead should be informed", example = "false", position = 3)
    private boolean notifyRegionalCTLead;
    @ApiModelProperty(value = "Indicates that there is data to indicate that this person has an increased risk of engaging in extremism", example = "false", position = 4)
    private boolean increasedRiskOfExtremism;

    public RiskType getRiskType() {
        return RiskType.EXTREMISM;
    }

    @Builder(builderMethodName = "extremismBuilder")
    public ExtremismProfile(@NotBlank String nomsId, @NotBlank String provisionalCategorisation, boolean notifyRegionalCTLead, boolean increasedRiskOfExtremism) {
        super(nomsId, provisionalCategorisation);
        this.notifyRegionalCTLead = notifyRegionalCTLead;
        this.increasedRiskOfExtremism = increasedRiskOfExtremism;
    }
}

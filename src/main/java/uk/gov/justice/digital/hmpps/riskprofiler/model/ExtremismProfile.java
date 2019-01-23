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

    @Builder(builderMethodName = "extremismBuilder")
    public ExtremismProfile(@NotBlank String nomsId, @NotBlank String provisionalCategorisation, boolean notifyRegionalCTLead) {
        super(nomsId, RiskType.EXTREMISM, provisionalCategorisation);
        this.notifyRegionalCTLead = notifyRegionalCTLead;
    }
}

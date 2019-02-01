package uk.gov.justice.digital.hmpps.riskprofiler.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class SocProfile extends RiskProfile {
    @ApiModelProperty(value = "Indicates the offender must be transferred to security", example = "true", position = 4)
    private boolean transferToSecurity;

    public RiskType getRiskType() {
        return RiskType.SOC;
    }

    @Builder(builderMethodName = "socBuilder")
    public SocProfile(@NotBlank String nomsId, @NotBlank String provisionalCategorisation, boolean transferToSecurity) {
        super(nomsId, provisionalCategorisation);
        this.transferToSecurity = transferToSecurity;
    }
}

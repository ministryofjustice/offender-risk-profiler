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

    @Builder(builderMethodName = "socBuilder")
    public SocProfile(@NotBlank String nomsId, @NotBlank String provisionalCategorisation, boolean transferToSecurity) {
        super(nomsId, RiskType.SOC, provisionalCategorisation);
        this.transferToSecurity = transferToSecurity;
    }
}

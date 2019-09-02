package uk.gov.justice.digital.hmpps.riskprofiler.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.commons.lang3.builder.CompareToBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SocProfile extends RiskProfile implements Comparable<SocProfile> {
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

    public int compareTo(@NotNull SocProfile socProfile) {
        return new CompareToBuilder()
                        .append(this.getRiskType(), socProfile.getRiskType())
                        .append(this.getNomsId(), socProfile.getNomsId())
                        .append(socProfile.isTransferToSecurity(), this.isTransferToSecurity())
                        .append(this.getProvisionalCategorisation(), socProfile.getProvisionalCategorisation())
                        .toComparison();
    }

}

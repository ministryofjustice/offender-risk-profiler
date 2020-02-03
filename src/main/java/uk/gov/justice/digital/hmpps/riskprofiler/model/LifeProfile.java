package uk.gov.justice.digital.hmpps.riskprofiler.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LifeProfile extends RiskProfile {

    @ApiModelProperty(value = "Indicates offender has a court-issued life sentence", example = "true", position = 1)
    private boolean life;

    public RiskType getRiskType() {
        return RiskType.LIFE;
    }

    public LifeProfile(@NotBlank final String nomsId, @NotBlank final String provisionalCategorisation, final boolean life) {
        super(nomsId, provisionalCategorisation);
        this.life = life;
    }
}

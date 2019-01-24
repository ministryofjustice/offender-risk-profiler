package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OCGM {
    private String nomisID;
    private String ocgmBand;
    private String standingWithinOcg;

    public static int getNomisIdPosition(){
        return 0;
    }
    public static int getOcgmBandPosition(){
        return 11;
    }
    public static int getStandingPosition(){
        return 12;
    }
    @Builder(builderMethodName = "ocgmModelBuilder")
    public OCGM(@NotBlank String nomisId, @NotBlank String ocgmBand, @NotBlank String standingWithinOcg) {
        this.nomisID = nomisId;
        this.ocgmBand = ocgmBand;
        this.standingWithinOcg = standingWithinOcg;
    }

}

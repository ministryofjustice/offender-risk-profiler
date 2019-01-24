package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class Pras {
    private String nomisId;

    public static int getNomisIdPosition(){
        return 11;
    }
    @Builder(builderMethodName = "prasModelBuilder")
    public Pras(@NotBlank String nomisId){
        this.nomisId = nomisId;
    }
}

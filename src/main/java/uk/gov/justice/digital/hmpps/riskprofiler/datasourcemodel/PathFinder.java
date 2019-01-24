package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class PathFinder {

    private String nomisId;
    private String pathFinderBanding;

    public static int getNomisIdPosition(){
        return 3;
    }
    public static int getPathFinderBindingPosition(){
        return 6;
    }
    @Builder(builderMethodName = "pathFinderModelBuilder")
    public PathFinder(@NotBlank String nomisId, @NotBlank String pathFinderBanding){
        this.nomisId = nomisId;
        this.pathFinderBanding = pathFinderBanding;
    }
}

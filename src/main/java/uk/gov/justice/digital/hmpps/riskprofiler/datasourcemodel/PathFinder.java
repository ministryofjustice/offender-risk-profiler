package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@EqualsAndHashCode(of = { "nomisId"})
@ToString
public class PathFinder {
    public static int NOMIS_ID_POSITION = 3;
    public static int PATH_FINDER_BINDING_POSITION = 6;

    private String nomisId;
    private String pathFinderBanding;


}

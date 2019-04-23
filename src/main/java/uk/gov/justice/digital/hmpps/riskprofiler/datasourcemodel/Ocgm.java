package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@Data
@EqualsAndHashCode(of = { "nomisId"})
@ToString
public class Ocgm  {
    public static int NOMIS_ID_POSITION = 0;
    public static int OCG_ID_POSITION = 4;
    public static int STANDING_POSITION = 25;

    private String nomisId;
    private String ocgId;
    private String standingWithinOcg;
}

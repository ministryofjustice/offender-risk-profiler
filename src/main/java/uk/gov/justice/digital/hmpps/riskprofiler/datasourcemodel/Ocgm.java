package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@Data
@EqualsAndHashCode(of = { "nomisId"})
@ToString
public class Ocgm {
    public static int NOMIS_ID_POSITION = 0;
    public static int OCGM_BAND_POSITION = 11;
    public static int STANDING_POSITION = 12;

    private String nomisId;
    private String ocgmBand;
    private String standingWithinOcg;
}

package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@Data
@EqualsAndHashCode(of = { "ocgId"})
@ToString
public class Ocg implements RiskDataSet {
    public static int OCG_ID_POSITION = 0;
    public static int OCGM_BAND_POSITION = 1;

    private String ocgId;
    private String ocgmBand;

    public String getKey() {
        return ocgId;
    }

}

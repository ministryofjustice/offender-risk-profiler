package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@Data
@EqualsAndHashCode(of = { "nomisId"})
@ToString
public class Ocgm implements RiskDataSet {
    public static int NOMIS_ID_POSITION = 0;
    public static int OCG_ID_POSITION = 1;
    public static int STANDING_POSITION = 8;

    private String nomisId;
    private String ocgId;
    private String standingWithinOcg;

    public String getKey() {
        return nomisId;
    }

    public int getKeyPosition() {
        return NOMIS_ID_POSITION;
    }

    @Override
    public FileType getFileType() {
        return FileType.OCGM;
    }
}

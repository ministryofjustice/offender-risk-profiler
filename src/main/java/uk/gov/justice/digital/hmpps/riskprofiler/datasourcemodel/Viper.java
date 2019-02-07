package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Data
@EqualsAndHashCode(of = { "nomisId"})
@ToString
public class Viper implements RiskDataSet {
    public static int NOMIS_ID_POSITION = 0;
    public static int SCORE_POSITION = 1;

    private String nomisId;
    private BigDecimal score;

    public String getKey() {
        return nomisId;
    }

    public int getKeyPosition() {
        return NOMIS_ID_POSITION;
    }

    @Override
    public FileType getFileType() {
        return FileType.VIPER;
    }
}

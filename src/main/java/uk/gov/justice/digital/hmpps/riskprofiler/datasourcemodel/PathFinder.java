package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@EqualsAndHashCode(of = { "nomisId"})
@ToString
public class PathFinder implements RiskDataSet {
    public static int NOMIS_ID_POSITION = 3;
    public static int PATH_FINDER_BANDING_POSITION = 6;

    private String nomisId;
    private String pathFinderBanding;

    public String getKey() {
        return nomisId;
    }

    public int getKeyPosition() {
        return NOMIS_ID_POSITION;
    }

    @Override
    public FileType getFileType() {
        return FileType.PATHFINDER;
    }
}

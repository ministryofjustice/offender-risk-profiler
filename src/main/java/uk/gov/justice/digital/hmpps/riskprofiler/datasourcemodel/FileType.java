package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import java.util.Arrays;

public enum FileType {
    PRAS(Pras.class),
    OCGM(OcgmList.class),
    OCG(Ocg.class),
    PATHFINDER(PathFinder.class),
    VIPER(Viper.class);

    private final Class<? extends RiskDataSet> type;

    FileType(Class<? extends RiskDataSet> type) {
        this.type = type;
    }

    public Class<? extends RiskDataSet> getType() {
        return type;
    }

    public static FileType byDataSet(Class<? extends RiskDataSet> clazz) {
        return Arrays.stream(FileType.values())
                .filter(ft -> ft.type == clazz)
                .findFirst().orElse(null);

    }

}

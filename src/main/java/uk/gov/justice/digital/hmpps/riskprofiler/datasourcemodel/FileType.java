package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

public enum FileType {
    PRAS(Pras.class),
    OCGM(Ocgm.class),
    OCG(Ocg.class),
    PATHFINDER(PathFinder.class),
    VIPER(Viper.class);

    private final Class<? extends RiskDataSet> type;

    FileType(Class<? extends RiskDataSet> type) {
        this.type = type;
    }

}

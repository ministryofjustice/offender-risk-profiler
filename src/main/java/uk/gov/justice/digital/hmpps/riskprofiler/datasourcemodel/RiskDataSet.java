package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

public interface RiskDataSet {

    FileType getFileType();

    String getKey();

    int getKeyPosition();
}

package uk.gov.justice.digital.hmpps.riskprofiler.services;

public interface FileService {

    PendingFile getLatestFile(String path);
}

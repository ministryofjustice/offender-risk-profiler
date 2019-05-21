package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;

@Component
@Slf4j
@ConditionalOnProperty(name = "file.process.type", havingValue = "file")
public class DefaultFileService implements FileService {

    public PendingFile getLatestFile(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            log.info("Found {} files in {}", listOfFiles.length, path);
            return Arrays.stream(listOfFiles)
                    .filter(f -> !f.isDirectory())
                    .max(Comparator.comparing(File::lastModified))
                    .map( f -> {
                        try {
                            return PendingFile.builder()
                                    .fileName(f.getName())
                                    .fileTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), ZoneId.systemDefault()))
                                    .data(IOUtils.toByteArray(new FileInputStream(f)))
                                    .build();
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .orElse(null);

        }
        log.info("Found {} files in {}", 0, path);
        return null;
    }

    @Override
    public void deleteHistoricalFiles(String fileLocation) {
        File folder = new File(fileLocation);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            log.info("Housekeeping- found {} files in {}", listOfFiles.length, fileLocation);
            Arrays.stream(listOfFiles).sorted(Comparator.comparing(File::lastModified).reversed()).skip(2).forEach(file -> {
                file.delete();
                log.info("Deleted file {} ", fileLocation + "/" + file.getName());
            });
        }
    }


}

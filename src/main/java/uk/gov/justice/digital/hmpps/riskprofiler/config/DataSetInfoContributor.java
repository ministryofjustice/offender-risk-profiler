package uk.gov.justice.digital.hmpps.riskprofiler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory;

import java.util.HashMap;

@Component
public class DataSetInfoContributor implements InfoContributor {

    private final DataRepositoryFactory dataRepositoryFactory;

    @Autowired
    public DataSetInfoContributor(DataRepositoryFactory dataRepositoryFactory) {
        this.dataRepositoryFactory = dataRepositoryFactory;
    }

    @Override
    public void contribute(Info.Builder builder) {
        var results = new HashMap<String, String>();

        dataRepositoryFactory.getRepositories().forEach(dataRepository -> {
            var data = dataRepository.getData();
            if (data.getFileType() != null) {
                results.put(data.getFileType().toString(), String.format("Processed %d, Dups %d, Invalid %d, Error %d, Total %d",
                        data.getLinesProcessed().get(), data.getLinesDup().get(), data.getLinesInvalid().get(), data.getLinesError().get(), data.getIndex().get()));
            }
        });

        builder.withDetail("riskData", results);
    }
}
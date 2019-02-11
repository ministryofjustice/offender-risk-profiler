package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.*;

import java.util.List;

@Component
public class DataRepositoryFactory {

    private final DataRepository<Ocgm> ocgmRepository;
    private final DataRepository<Ocg> ocgRepository;
    private final DataRepository<PathFinder> pathfinderRepository;
    private final DataRepository<Pras> prasRepository;
    private final DataRepository<Viper> viperRepository;

    public DataRepositoryFactory(OcgmRepository ocgmRepository, OcgRepository ocgRepository, PathfinderRepository pathfinderRepository, PrasRepository prasRepository, ViperRepository viperRepository) {
        this.ocgmRepository = ocgmRepository;
        this.ocgRepository = ocgRepository;
        this.pathfinderRepository = pathfinderRepository;
        this.prasRepository = prasRepository;
        this.viperRepository = viperRepository;
    }

    @SuppressWarnings("unchecked")
    public <T extends RiskDataSet> DataRepository<T> getRepository(Class<T> type) {

        DataRepository<T> repository = null;

        switch (FileType.byDataSet(type)) {

            case PRAS:
                return (DataRepository<T>)prasRepository;

            case OCGM:
                return (DataRepository<T>)ocgmRepository;

            case OCG:
                return (DataRepository<T>)ocgRepository;

            case PATHFINDER:
                return (DataRepository<T>)pathfinderRepository;

            case VIPER:
                return (DataRepository<T>)viperRepository;
        }

        return repository;
    }

    public List<DataRepository<? extends RiskDataSet>> getRepositories() {
        return List.of(
                getRepository(Pras.class),
                getRepository(Viper.class),
                getRepository(Ocg.class),
                getRepository(Ocgm.class),
                getRepository(PathFinder.class));
    }
}

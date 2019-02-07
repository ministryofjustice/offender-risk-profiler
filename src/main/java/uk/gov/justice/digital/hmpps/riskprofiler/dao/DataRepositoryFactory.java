package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.*;

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

    public DataRepository<? extends RiskDataSet> getRepository(FileType type) {

        DataRepository<? extends RiskDataSet> repository = null;

        switch (type) {

            case PRAS:
                return prasRepository;

            case OCGM:
                return ocgmRepository;

            case OCG:
                return ocgRepository;

            case PATHFINDER:
                return pathfinderRepository;

            case VIPER:
                return viperRepository;
        }

        return repository;
    }
}

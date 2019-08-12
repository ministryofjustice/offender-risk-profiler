package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrisonSupportedRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PrisonSupported;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PrisonService {

    private final PrisonSupportedRepository repository;

    @Autowired
    public PrisonService(final PrisonSupportedRepository repository) {
        this.repository = repository;
    }

    public List<PrisonSupported> getPrisons() {
        return repository.findAll();
    }
}

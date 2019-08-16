package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrisonSupportedRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.exception.PrisonException;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PrisonSupported;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PrisonService {

    private final PrisonSupportedRepository repository;
    private final NomisService nomisService;

    @Autowired
    public PrisonService(final PrisonSupportedRepository repository, NomisService nomisService) {
        this.repository = repository;
        this.nomisService = nomisService;
    }

    public List<PrisonSupported> getPrisons() {
        return repository.findAll();
    }

    @Transactional
    public void addPrison(String prisonId) {
        if (repository.existsById(prisonId)) {
            throw PrisonException.exists(prisonId);
        }
        final List<String> offendersAtPrison = nomisService.getOffendersAtPrison(prisonId);
        if (offendersAtPrison.isEmpty()) {
            throw PrisonException.withId(prisonId);
        }
        repository.save(PrisonSupported.builder().prisonId(prisonId).startDateTime(LocalDateTime.now()).build());
    }
}

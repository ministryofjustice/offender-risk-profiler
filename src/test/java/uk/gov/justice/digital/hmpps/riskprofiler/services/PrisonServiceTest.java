package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrisonSupportedRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.exception.PrisonException;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PrisonSupported;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrisonServiceTest {

    private final static String PRISON = "TEST";

    @Mock
    private PrisonSupportedRepository repository;
    @Mock
    private NomisService nomisService;

    private PrisonService prisonService;

    @Before
    public void init() {
        prisonService = new PrisonService(repository, nomisService);
    }

    @Test
    public void addPrisonHappy() {
        when(repository.existsById(PRISON)).thenReturn(false);
        when(nomisService.getOffendersAtPrison(PRISON)).thenReturn(Arrays.asList("offender1"));
        prisonService.addPrison(PRISON);
        verify(repository).save(any(PrisonSupported.class));
    }

    @Test
    public void addPrisonWhichDoesNotExist() {
        when(repository.existsById(PRISON)).thenReturn(false);
        when(nomisService.getOffendersAtPrison(PRISON)).thenReturn(Collections.emptyList());
        try {
            prisonService.addPrison(PRISON);
            fail("Exception should have been thrown");
        } catch (PrisonException e) {
            assertThat(e.getMessage()).isEqualTo("Prison [TEST] is invalid.");
        }
        verify(repository, never()).save(any());
    }

    @Test
    public void addPrisonWhichIsAlreadyPresent() {
        when(repository.existsById(PRISON)).thenReturn(true);
        try {
            prisonService.addPrison(PRISON);
            fail("Exception should have been thrown");
        } catch (PrisonException e) {
            assertThat(e.getMessage()).isEqualTo("Prison [TEST] is already present.");
        }
        verify(repository, never()).save(any());
    }
}

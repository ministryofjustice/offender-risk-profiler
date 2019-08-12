package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PreviousProfileRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.ExtremismProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PreviousProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Status;
import uk.gov.justice.digital.hmpps.riskprofiler.model.ViolenceProfile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.lang.Math.abs;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PollPrisonersServiceTest {

    private static final String OFFENDER_1 = "AB1234A";
    private PollPrisonersService service;

    @Mock
    private SocDecisionTreeService socDecisionTreeService;
    @Mock
    private ViolenceDecisionTreeService violenceDecisionTreeService;
    @Mock
    private EscapeDecisionTreeService escapeDecisionTreeService;
    @Mock
    private ExtremismDecisionTreeService extremismDecisionTreeService;
    @Mock
    private PreviousProfileRepository previousProfileRepository;
    @Mock
    private TelemetryClient telemetryClient;

    final SocProfile SOC_1 = SocProfile.socBuilder().nomsId(OFFENDER_1).build();
    final ViolenceProfile VIOLENCE_1 = ViolenceProfile.violenceBuilder().nomsId(OFFENDER_1).build();
    final EscapeProfile ESCAPE_1 = EscapeProfile.escapeBuilder().nomsId(OFFENDER_1).build();
    final ExtremismProfile EXTREMISM_1 = ExtremismProfile.extremismBuilder().nomsId(OFFENDER_1).build();
    final PreviousProfile PROFILE_1 = PreviousProfile.builder()
            .offenderNo(OFFENDER_1)
            .soc("{\"nomsId\":\"" + OFFENDER_1 + "\",\"transferToSecurity\":false,\"riskType\":\"SOC\"}")
            .violence("{\"nomsId\":\"" + OFFENDER_1 + "\",\"veryHighRiskViolentOffender\":false,\"notifySafetyCustodyLead\":false,\"displayAssaults\":false,\"numberOfAssaults\":0,\"numberOfSeriousAssaults\":0,\"riskType\":\"VIOLENCE\"}")
            .escape("{\"nomsId\":\"" + OFFENDER_1 + "\",\"activeEscapeList\":false,\"activeEscapeRisk\":false,\"riskType\":\"ESCAPE\"}")
            .extremism("{\"nomsId\":\"" + OFFENDER_1 + "\",\"notifyRegionalCTLead\":false,\"increasedRiskOfExtremism\":false,\"riskType\":\"EXTREMISM\"}")
            .executeDateTime(LocalDateTime.now())
            .status(Status.NEW)
            .build();

    @Before
    public void setup() {
        service = new PollPrisonersService(socDecisionTreeService,
                violenceDecisionTreeService,
                escapeDecisionTreeService,
                extremismDecisionTreeService,
                previousProfileRepository,
                telemetryClient);
    }

    @Test
    public void testChangeSoc() {
        when(socDecisionTreeService.getSocData(OFFENDER_1)).thenReturn(SocProfile.socBuilder().nomsId(OFFENDER_1).transferToSecurity(true).build());
        when(violenceDecisionTreeService.getViolenceProfile(OFFENDER_1)).thenReturn(VIOLENCE_1);
        when(escapeDecisionTreeService.getEscapeProfile(OFFENDER_1)).thenReturn(ESCAPE_1);
        when(extremismDecisionTreeService.getExtremismProfile(OFFENDER_1, false)).thenReturn(EXTREMISM_1);
        when(previousProfileRepository.findById(OFFENDER_1)).thenReturn(Optional.of(PROFILE_1));

        service.pollPrisoner(OFFENDER_1);

        // TODO verify call to add to queue etc
        verify(previousProfileRepository, never()).save(any());
    }

    @Test
    public void testNoChange() {
        when(socDecisionTreeService.getSocData(OFFENDER_1)).thenReturn(SOC_1);
        when(violenceDecisionTreeService.getViolenceProfile(OFFENDER_1)).thenReturn(VIOLENCE_1);
        when(escapeDecisionTreeService.getEscapeProfile(OFFENDER_1)).thenReturn(ESCAPE_1);
        when(extremismDecisionTreeService.getExtremismProfile(OFFENDER_1, false)).thenReturn(EXTREMISM_1);
        when(previousProfileRepository.findById(OFFENDER_1)).thenReturn(Optional.of(PROFILE_1));

        service.pollPrisoner(OFFENDER_1);

        // TODO verify no queue call etc
        verify(previousProfileRepository, never()).save(any());
    }

    @Test
    public void testNewData() {
        when(socDecisionTreeService.getSocData(OFFENDER_1)).thenReturn(SOC_1);
        when(violenceDecisionTreeService.getViolenceProfile(OFFENDER_1)).thenReturn(VIOLENCE_1);
        when(escapeDecisionTreeService.getEscapeProfile(OFFENDER_1)).thenReturn(ESCAPE_1);
        when(extremismDecisionTreeService.getExtremismProfile(OFFENDER_1, false)).thenReturn(EXTREMISM_1);
        when(previousProfileRepository.findById(OFFENDER_1)).thenReturn(Optional.empty());

        service.pollPrisoner(OFFENDER_1);

        verify(socDecisionTreeService).getSocData(OFFENDER_1);
        verify(previousProfileRepository).save(eqProfiles(PROFILE_1));
    }

    private static PreviousProfile eqProfiles(PreviousProfile profile) {
        return argThat(argument ->
                profile.getSoc().equals(argument.getSoc())
                        && profile.getViolence().equals(argument.getViolence())
                        && profile.getEscape().equals(argument.getEscape())
                        && profile.getExtremism().equals(argument.getExtremism())
                        && abs(ChronoUnit.SECONDS.between(profile.getExecuteDateTime(), argument.getExecuteDateTime())) < 2
                        && profile.getStatus().equals(argument.getStatus()));
    }
}

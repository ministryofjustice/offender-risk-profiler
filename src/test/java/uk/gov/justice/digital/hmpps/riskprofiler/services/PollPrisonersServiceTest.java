package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PreviousProfileRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.abs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

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
    private PreviousProfileRepository previousProfileRepository;
    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private SQSService sqsService;

    private final SocProfile SOC_1 = SocProfile.socBuilder().nomsId(OFFENDER_1).provisionalCategorisation("C").build();
    private final ViolenceProfile VIOLENCE_1 = ViolenceProfile.violenceBuilder().nomsId(OFFENDER_1).provisionalCategorisation("C").build();
    private final EscapeProfile ESCAPE_1 = EscapeProfile.escapeBuilder().nomsId(OFFENDER_1).provisionalCategorisation("C").build();
    private final PreviousProfile PROFILE_1 = PreviousProfile.builder()
            .offenderNo(OFFENDER_1)
            .soc("{\"nomsId\":\"" + OFFENDER_1 + "\",\"provisionalCategorisation\":\"C\",\"transferToSecurity\":false,\"riskType\":\"SOC\"}")
            .violence("{\"nomsId\":\"" + OFFENDER_1 + "\",\"provisionalCategorisation\":\"C\",\"veryHighRiskViolentOffender\":false,\"notifySafetyCustodyLead\":false,\"displayAssaults\":false,\"numberOfAssaults\":0,\"numberOfSeriousAssaults\":0,\"numberOfNonSeriousAssaults\":0,\"riskType\":\"VIOLENCE\"}")
            .escape("{\"nomsId\":\"" + OFFENDER_1 + "\",\"provisionalCategorisation\":\"C\",\"activeEscapeList\":false,\"activeEscapeRisk\":false,\"riskType\":\"ESCAPE\"}")
            .executeDateTime(LocalDateTime.now())
            .build();

    @Before
    public void setup() {
        service = new PollPrisonersService(socDecisionTreeService,
                violenceDecisionTreeService,
                escapeDecisionTreeService,
                previousProfileRepository,
                telemetryClient,
                sqsService);
    }

    @Test
    public void testChangeSoc() {
        when(socDecisionTreeService.getSocData(OFFENDER_1)).thenReturn(SocProfile.socBuilder().nomsId(OFFENDER_1).transferToSecurity(true).build());
        when(violenceDecisionTreeService.getViolenceProfile(OFFENDER_1)).thenReturn(VIOLENCE_1);
        when(escapeDecisionTreeService.getEscapeProfile(OFFENDER_1)).thenReturn(ESCAPE_1);
        when(previousProfileRepository.findById(OFFENDER_1)).thenReturn(Optional.of(PROFILE_1));

        service.pollPrisoner(OFFENDER_1);

        var rpc = RiskProfileChange.builder()
                .newProfile(
                        ProfileMessagePayload.builder()
                                .soc(SocProfile.socBuilder().transferToSecurity(true).nomsId(OFFENDER_1).build())
                                .escape(ESCAPE_1)
                                .violence(VIOLENCE_1)
                                .build())
                .oldProfile(
                        ProfileMessagePayload.builder()
                                .soc(SOC_1)
                                .escape(ESCAPE_1)
                                .violence(VIOLENCE_1)
                                .build())
                .executeDateTime(LocalDateTime.now())
                .offenderNo(OFFENDER_1).build();

        verify(sqsService).sendRiskProfileChangeMessage(eqRiskProfileChange(rpc));
        verify(previousProfileRepository, never()).save(any());
    }

    @Test
    public void testNoChange() {
        when(socDecisionTreeService.getSocData(OFFENDER_1)).thenReturn(SOC_1);
        when(violenceDecisionTreeService.getViolenceProfile(OFFENDER_1)).thenReturn(VIOLENCE_1);
        when(escapeDecisionTreeService.getEscapeProfile(OFFENDER_1)).thenReturn(ESCAPE_1);
        when(previousProfileRepository.findById(OFFENDER_1)).thenReturn(Optional.of(PROFILE_1));

        service.pollPrisoner(OFFENDER_1);

        verify(sqsService, never()).sendRiskProfileChangeMessage(any());
        verify(previousProfileRepository, never()).save(any());
    }

    @Test
    public void testNewData() {
        when(socDecisionTreeService.getSocData(OFFENDER_1)).thenReturn(SOC_1);
        when(violenceDecisionTreeService.getViolenceProfile(OFFENDER_1)).thenReturn(VIOLENCE_1);
        when(escapeDecisionTreeService.getEscapeProfile(OFFENDER_1)).thenReturn(ESCAPE_1);
        when(previousProfileRepository.findById(OFFENDER_1)).thenReturn(Optional.empty());

        service.pollPrisoner(OFFENDER_1);

        verify(socDecisionTreeService).getSocData(OFFENDER_1);
        verify(previousProfileRepository).save(eqProfiles(PROFILE_1));
        verify(sqsService, never()).sendRiskProfileChangeMessage(any());
    }

    private static PreviousProfile eqProfiles(PreviousProfile profile) {
        return argThat(argument ->
                profile.getSoc().equals(argument.getSoc())
                        && profile.getViolence().equals(argument.getViolence())
                        && profile.getEscape().equals(argument.getEscape())
                        && abs(ChronoUnit.SECONDS.between(profile.getExecuteDateTime(), argument.getExecuteDateTime())) < 2);
    }

    private static RiskProfileChange eqRiskProfileChange(RiskProfileChange rpc) {
        return argThat(argument ->
                rpc.getNewProfile().equals(argument.getNewProfile())
                && rpc.getOldProfile().equals(argument.getOldProfile())
                        && abs(ChronoUnit.SECONDS.between(rpc.getExecuteDateTime(), argument.getExecuteDateTime())) < 2
                        && rpc.getOffenderNo().equals(argument.getOffenderNo()));
    }
}

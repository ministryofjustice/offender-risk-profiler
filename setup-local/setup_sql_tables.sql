
-- V1_1__create_previous_profile.sql

CREATE TABLE risk_profiler.PREVIOUS_PROFILE
(
    OFFENDER_NO       VARCHAR(10) PRIMARY KEY,
    ESCAPE            TEXT,
    EXTREMISM         TEXT,
    SOC               TEXT,
    VIOLENCE          TEXT,
    EXECUTE_DATE_TIME TIMESTAMP   NOT NULL,
    STATUS            VARCHAR(20) NOT NULL
);

COMMENT ON TABLE risk_profiler.PREVIOUS_PROFILE IS 'Records the previous risk profile results for an offender';

GRANT SELECT, INSERT, UPDATE ON risk_profiler.PREVIOUS_PROFILE TO risk_profiler;

-- V1_2__prison_supported.sql

CREATE TABLE PRISON_SUPPORTED
(
    PRISON_ID       VARCHAR(6) PRIMARY KEY,
    START_DATE_TIME TIMESTAMP NOT NULL
);

COMMENT ON TABLE risk_profiler.PRISON_SUPPORTED IS 'Records the prisons which have started using risk profiler';

COMMENT ON COLUMN risk_profiler.PRISON_SUPPORTED.PRISON_ID IS 'Prison ID (NOMIS column is Agency ID)';
COMMENT ON COLUMN risk_profiler.PRISON_SUPPORTED.START_DATE_TIME IS 'Indicates when the prison started using risk profiler';

GRANT SELECT, INSERT, UPDATE ON risk_profiler.PRISON_SUPPORTED TO risk_profiler;

-- V1_3__shedlock.sql

CREATE TABLE SHEDLOCK
(
    name       VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP(3) NULL,
    locked_at  TIMESTAMP(3) NULL,
    locked_by  VARCHAR(255) NOT NULL
);

GRANT SELECT, INSERT, UPDATE, DELETE ON risk_profiler.SHEDLOCK TO risk_profiler;

-- V1_4__previous_profile_drop_status.sql

ALTER TABLE public.PREVIOUS_PROFILE drop column STATUS;


-- data from dev

INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('BAI', '2019-08-27 12:59:59.640');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('BWI', '2019-09-13 17:25:41.307');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('FNI', '2019-09-13 17:26:45.666');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('LEI', '2019-09-13 17:26:53.725');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('LPI', '2019-09-13 17:27:25.737');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('MDI', '2019-09-13 17:27:37.097');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('NMI', '2019-09-13 17:28:04.625');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('RNI', '2019-09-13 17:29:13.036');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('WYI', '2019-09-13 17:29:37.927');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('WRI', '2019-09-13 17:30:10.419');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('PBI', '2022-12-07 14:43:30.592');
INSERT INTO risk_profiler.prison_supported
(prison_id, start_date_time)
VALUES('FKI', '2023-01-03 13:30:57.204');


INSERT INTO risk_profiler.previous_profile
(offender_no, "escape", extremism, soc, violence, execute_date_time)
VALUES('G2194GK', '{"nomsId":"G2194GK","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":false,"escapeRiskAlerts":[],"escapeListAlerts":[],"riskType":"ESCAPE"}', '{"nomsId":"G2194GK","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}', '{"nomsId":"G2194GK","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}', '{"nomsId":"G2194GK","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":false,"numberOfAssaults":0,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}', '2021-07-27 02:18:22.106');
INSERT INTO risk_profiler.previous_profile
(offender_no, "escape", extremism, soc, violence, execute_date_time)
VALUES('G4485UD', '{"nomsId":"G4485UD","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":false,"escapeRiskAlerts":[],"escapeListAlerts":[],"riskType":"ESCAPE"}', '{"nomsId":"G4485UD","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}', '{"nomsId":"G4485UD","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}', '{"nomsId":"G4485UD","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":false,"numberOfAssaults":0,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}', '2021-07-27 02:15:26.668');
INSERT INTO risk_profiler.previous_profile
(offender_no, "escape", extremism, soc, violence, execute_date_time)
VALUES('G2969UP', '{"nomsId":"G2969UP","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":false,"escapeRiskAlerts":[],"escapeListAlerts":[],"riskType":"ESCAPE"}', '{"nomsId":"G2969UP","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}', '{"nomsId":"G2969UP","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}', '{"nomsId":"G2969UP","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":false,"numberOfAssaults":0,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}', '2021-07-27 02:15:26.693');
INSERT INTO risk_profiler.previous_profile
(offender_no, "escape", extremism, soc, violence, execute_date_time)
VALUES('G2466UP', '{"nomsId":"G2466UP","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":false,"escapeRiskAlerts":[],"escapeListAlerts":[],"riskType":"ESCAPE"}', '{"nomsId":"G2466UP","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}', '{"nomsId":"G2466UP","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}', '{"nomsId":"G2466UP","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":false,"numberOfAssaults":0,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}', '2021-07-27 02:15:26.711');
INSERT INTO risk_profiler.previous_profile
(offender_no, "escape", extremism, soc, violence, execute_date_time)
VALUES('G3443VN', '{"nomsId":"G3443VN","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":false,"escapeRiskAlerts":[],"escapeListAlerts":[],"riskType":"ESCAPE"}', '{"nomsId":"G3443VN","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}', '{"nomsId":"G3443VN","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}', '{"nomsId":"G3443VN","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":true,"numberOfAssaults":9,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}', '2021-07-27 02:15:26.775');
INSERT INTO risk_profiler.previous_profile
(offender_no, "escape", extremism, soc, violence, execute_date_time)
VALUES('G7382GE', '{"nomsId":"G7382GE","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":false,"escapeRiskAlerts":[],"escapeListAlerts":[],"riskType":"ESCAPE"}', '{"nomsId":"G7382GE","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}', '{"nomsId":"G7382GE","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}', '{"nomsId":"G7382GE","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":false,"numberOfAssaults":0,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}', '2021-07-27 02:15:26.731');
INSERT INTO risk_profiler.previous_profile
(offender_no, "escape", extremism, soc, violence, execute_date_time)
VALUES('G3951UQ', '{"nomsId":"G3951UQ","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":false,"escapeRiskAlerts":[],"escapeListAlerts":[],"riskType":"ESCAPE"}', '{"nomsId":"G3951UQ","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}', '{"nomsId":"G3951UQ","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}', '{"nomsId":"G3951UQ","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":true,"numberOfAssaults":1,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}', '2021-07-27 02:15:26.753');
INSERT INTO risk_profiler.previous_profile
(offender_no, "escape", extremism, soc, violence, execute_date_time)
VALUES('G1123GI', '{"nomsId":"G1123GI","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":false,"escapeRiskAlerts":[],"escapeListAlerts":[],"riskType":"ESCAPE"}', '{"nomsId":"G1123GI","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}', '{"nomsId":"G1123GI","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}', '{"nomsId":"G1123GI","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":true,"numberOfAssaults":2,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}', '2021-07-27 02:15:26.795');
INSERT INTO risk_profiler.previous_profile
(offender_no, "escape", extremism, soc, violence, execute_date_time)
VALUES('G9345GO', '{"nomsId":"G9345GO","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":false,"escapeRiskAlerts":[],"escapeListAlerts":[],"riskType":"ESCAPE"}', '{"nomsId":"G9345GO","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}', '{"nomsId":"G9345GO","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}', '{"nomsId":"G9345GO","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":false,"numberOfAssaults":0,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}', '2021-07-27 02:17:15.977');
INSERT INTO risk_profiler.previous_profile
(offender_no, "escape", extremism, soc, violence, execute_date_time)
VALUES('G6354VN', '{"nomsId":"G6354VN","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":false,"escapeRiskAlerts":[],"escapeListAlerts":[],"riskType":"ESCAPE"}', '{"nomsId":"G6354VN","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}', '{"nomsId":"G6354VN","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}', '{"nomsId":"G6354VN","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":true,"numberOfAssaults":7,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}', '2021-07-27 02:15:26.609');


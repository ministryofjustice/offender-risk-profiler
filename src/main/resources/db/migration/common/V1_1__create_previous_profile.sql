CREATE TABLE PREVIOUS_PROFILE
(
    OFFENDER_NO       VARCHAR(10) PRIMARY KEY,
    ESCAPE            TEXT,
    EXTREMISM         TEXT,
    SOC               TEXT,
    VIOLENCE          TEXT,
    EXECUTE_DATE_TIME TIMESTAMP   NOT NULL,
    STATUS            VARCHAR(20) NOT NULL
);

COMMENT ON TABLE PREVIOUS_PROFILE IS 'Records the previous risk profile results for an offender';

GRANT SELECT, INSERT, UPDATE ON PREVIOUS_PROFILE TO risk_profiler;

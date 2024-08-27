
-- V1_1__create_previous_profile.sql

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

COMMENT ON TABLE public.PREVIOUS_PROFILE IS 'Records the previous risk profile results for an offender';

GRANT SELECT, INSERT, UPDATE ON public.PREVIOUS_PROFILE TO risk_profiler;

-- V1_2__prison_supported.sql

CREATE TABLE PRISON_SUPPORTED
(
    PRISON_ID       VARCHAR(6) PRIMARY KEY,
    START_DATE_TIME TIMESTAMP NOT NULL
);

COMMENT ON TABLE public.PRISON_SUPPORTED IS 'Records the prisons which have started using risk profiler';

COMMENT ON COLUMN public.PRISON_SUPPORTED.PRISON_ID IS 'Prison ID (NOMIS column is Agency ID)';
COMMENT ON COLUMN public.PRISON_SUPPORTED.START_DATE_TIME IS 'Indicates when the prison started using risk profiler';

GRANT SELECT, INSERT, UPDATE ON public.PRISON_SUPPORTED TO risk_profiler;

-- V1_3__shedlock.sql

CREATE TABLE SHEDLOCK
(
    name       VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP(3) NULL,
    locked_at  TIMESTAMP(3) NULL,
    locked_by  VARCHAR(255) NOT NULL
);

GRANT SELECT, INSERT, UPDATE, DELETE ON public.SHEDLOCK TO risk_profiler;

-- V1_4__previous_profile_drop_status.sql

ALTER TABLE public.PREVIOUS_PROFILE drop column STATUS;
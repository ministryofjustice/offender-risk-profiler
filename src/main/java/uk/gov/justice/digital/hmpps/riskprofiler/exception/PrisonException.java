package uk.gov.justice.digital.hmpps.riskprofiler.exception;

import java.util.function.Supplier;

public class PrisonException extends RuntimeException implements Supplier<PrisonException> {
    private static final String INVALID_PRISON = "Prison [%s] is invalid.";
    private static final String PRISON_EXISTS = "Prison [%s] is already present.";

    public static PrisonException exists(String id) {
        return new PrisonException(String.format(PRISON_EXISTS, id));
    }

    public static PrisonException withId(String id) {
        return new PrisonException(String.format(INVALID_PRISON, id));
    }

    public static PrisonException withMessage(String message) {
        return new PrisonException(message);
    }

    public static PrisonException withMessage(String message, Object... args) {
        return new PrisonException(String.format(message, args));
    }

    public PrisonException(String message) {
        super(message);
    }

    @Override
    public PrisonException get() {
        return new PrisonException(getMessage());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}

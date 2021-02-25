package uk.gov.justice.digital.hmpps.riskprofiler.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.digital.hmpps.riskprofiler.model.ErrorResponse;


@org.springframework.web.bind.annotation.RestControllerAdvice(
        basePackageClasses = {RiskProfilerResource.class, BatchHelperResource.class}
)
@Slf4j
public class ControllerAdvice {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<byte[]> handleException(WebClientResponseException e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
                .status(e.getRawStatusCode())
                .body(e.getResponseBodyAsByteArray());
    }

    @ExceptionHandler(WebClientException.class)
    public ResponseEntity<ErrorResponse> handleException(WebClientException e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse
                        .builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .developerMessage(e.getMessage())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleException(AccessDeniedException e) {
        log.debug("Forbidden (403) returned", e);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse
                        .builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse
                        .builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .developerMessage(e.getMessage())
                        .build());
    }
}

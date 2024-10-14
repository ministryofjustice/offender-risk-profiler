package uk.gov.justice.digital.hmpps.riskprofiler.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.riskprofiler.model.ErrorResponse

@RestControllerAdvice(basePackageClasses = [RiskProfilerResource::class, BatchHelperResource::class])
class ControllerAdvice {
  @ExceptionHandler(WebClientResponseException::class)
  fun handleException(e: WebClientResponseException): ResponseEntity<ByteArray> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(e.statusCode)
      .body(e.responseBodyAsByteArray)
  }

  @ExceptionHandler(WebClientException::class)
  fun handleException(e: WebClientException): ResponseEntity<ErrorResponse> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          e.message,
        ),
      )
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleException(e: AccessDeniedException?): ResponseEntity<ErrorResponse> {
    log.debug("Forbidden (403) returned", e)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ErrorResponse(HttpStatus.FORBIDDEN.value(), null))
  }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.message))
  }

  companion object {
    private val log = LoggerFactory.getLogger(ControllerAdvice::class.java)
  }
}

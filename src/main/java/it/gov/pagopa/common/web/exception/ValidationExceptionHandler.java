package it.gov.pagopa.common.web.exception;

import tools.jackson.databind.exc.InvalidFormatException;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MissingRequestValueException;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidationExceptionHandler {

    private final ErrorDTO templateValidationErrorDTO;

    public ValidationExceptionHandler(@Nullable ErrorDTO templateValidationErrorDTO) {
        this.templateValidationErrorDTO = Optional.ofNullable(templateValidationErrorDTO)
                .orElse(new ErrorDTO("INVALID_REQUEST", "Invalid request"));
    }

    /**
     * Handles validation errors that occur during request binding.
     *
     * @param ex the WebExchangeBindException containing validation errors
     * @param request the ServerHttpRequest being processed
     * @return an ErrorDTO containing details about the validation errors
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleWebExchangeBindException(
            WebExchangeBindException ex, ServerHttpRequest request) {

        String message = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return String.format("[%s]: %s", fieldName, errorMessage);
                }).collect(Collectors.joining("; "));

        log.info("A WebExchangeBindException occurred handling request {}: HttpStatus 400 - {}",
                ErrorManager.getRequestDetails(request), message);
        log.debug("Something went wrong while validating http request", ex);

        return new ErrorDTO(templateValidationErrorDTO.getCode(), message);
    }

    /**
     * Handles MissingRequestValueException that occur during request processing.
     *
     * @param e the MissingRequestValueException
     * @param request the ServerHttpRequest being processed
     * @return an ErrorDTO indicating a missing request value
     */
    @ExceptionHandler(MissingRequestValueException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleMissingRequestValueException(MissingRequestValueException e, ServerHttpRequest request) {

        log.info("A MissingRequestValueException occurred handling request {}: HttpStatus 400 - {}",
                ErrorManager.getRequestDetails(request), e.getMessage());
        log.debug("Something went wrong due to a missing request value", e);

        return new ErrorDTO(templateValidationErrorDTO.getCode(), templateValidationErrorDTO.getMessage());
    }

    /**
     * Handles NoResourceFoundException that occur during request processing.
     *
     * @param e the NoResourceFoundException
     * @param request the ServerHttpRequest being processed
     * @return an ErrorDTO indicating that no resource was found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleNoResourceFoundException(NoResourceFoundException e, ServerHttpRequest request) {

        log.info("A NoResourceFoundException occurred handling request {}: HttpStatus 400 - {}",
                ErrorManager.getRequestDetails(request), e.getMessage());
        log.debug("Something went wrong due to a missing request value", e);

        return new ErrorDTO(templateValidationErrorDTO.getCode(), templateValidationErrorDTO.getMessage());
    }

    /**
     * Handles errors that occur when the request body cannot be decoded.
     *
     * @param ex the DecodingException containing the decoding error details
     * @param request the ServerHttpRequest being processed
     * @return an ErrorDTO containing details about the decoding error
     */
    @ExceptionHandler(DecodingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleDecodingException(DecodingException ex, ServerHttpRequest request) {

        String message = templateValidationErrorDTO.getMessage();
        Throwable cause = ex.getCause();

        // DecodingException wrap InvalidFormatException
        if (cause instanceof InvalidFormatException ife) {
                String fieldName = ife.getPath().stream()
                    .map(ref -> ref.getPropertyName() != null ? ref.getPropertyName() : "[" + ref.getIndex() + "]")
                    .collect(Collectors.joining("."));
                    
                    fieldName = fieldName.isEmpty() ? "unknown" : fieldName;

            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String validValue = Arrays.stream(ife.getTargetType().getEnumConstants())
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
                message = String.format(
                    "[%s]: must be one of [%s]",
                    fieldName, validValue
                );
            } else {
                message = String.format(
                    "[%s]: invalid value for type %s",
                    fieldName,
                    ife.getTargetType() != null ? ife.getTargetType().getSimpleName() : "unknown"
                );
            }
        }

        log.info("A DecodingException occurred handling request {}: HttpStatus 400 - {}",
            ErrorManager.getRequestDetails(request), message);
        log.debug("Something went wrong while reading http request body", ex);

        return new ErrorDTO(templateValidationErrorDTO.getCode(), message);
    }

    /**
     * Handles UnsupportedMediaTypeStatusException that occur during request processing.
     *
     * @param ex the UnsupportedMediaTypeStatusException
     * @param request the ServerHttpRequest being processed
     * @return an ErrorDTO indicating an unsupported media type
     */
    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorDTO handleUnsupportedMediaTypeStatusException(
        UnsupportedMediaTypeStatusException ex, ServerHttpRequest request) {

        String supportedMedia = ex.getSupportedMediaTypes().stream()
            .map(String::valueOf)
            .collect(Collectors.joining(", "));

        String message = String.format(
            "Content-Type '%s' not supported. Accepted: [%s]",
            ex.getContentType(), supportedMedia
        );

        log.info("A UnsupportedMediaTypeStatusException occurred handling request {}: HttpStatus 415 - {}",
            ErrorManager.getRequestDetails(request), message);
        log.debug("Something went wrong due to unsupported media type", ex);

        return new ErrorDTO(templateValidationErrorDTO.getCode(), message);
    }

}

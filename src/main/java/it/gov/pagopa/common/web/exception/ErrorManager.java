package it.gov.pagopa.common.web.exception;

import it.gov.pagopa.common.web.dto.ErrorDTO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mongodb.MongoCommandException;

import java.util.Optional;

@RestControllerAdvice
@Slf4j
public class ErrorManager {
  private final ErrorDTO defaultErrorDTO;

  public ErrorManager(@Nullable ErrorDTO defaultErrorDTO) {
    this.defaultErrorDTO = Optional.ofNullable(defaultErrorDTO)
            .orElse(new ErrorDTO("Error", "Something gone wrong"));
  }

  /**
   * Handles RuntimeException and its subclasses thrown during request processing.
   * <p>
   * This method applies different handling strategies based on the exception type:
   * <ul>
   *   <li>{@link ClientExceptionNoBody}: Returns an empty response with the exception's HTTP status</li>
   *   <li>{@link ClientExceptionWithBody}: Returns a JSON error response with custom code and message</li>
   *   <li>Other RuntimeExceptions: Returns a 500 Internal Server Error with default error details</li>
   * </ul>
   * </p>
   *
   * @param error the RuntimeException to handle
   * @param request the current HTTP request for context and logging purposes
   * @return a ResponseEntity containing:
   *         <ul>
   *           <li>No body for {@link ClientExceptionNoBody}</li>
   *           <li>An {@link ErrorDTO} with error code and message for {@link ClientExceptionWithBody}</li>
   *           <li>A default {@link ErrorDTO} for other RuntimeExceptions (HTTP 500)</li>
   *         </ul>
   */
  @ExceptionHandler(RuntimeException.class)
  protected ResponseEntity<ErrorDTO> handleException(RuntimeException error, ServerHttpRequest request) {

    logClientException(error, request);

    if(error instanceof ClientExceptionNoBody clientExceptionNoBody){
      return ResponseEntity.status(clientExceptionNoBody.getHttpStatus()).build();
    }
    else {
      ErrorDTO errorDTO;
      HttpStatus httpStatus;
      if (error instanceof ClientExceptionWithBody clientExceptionWithBody){
        httpStatus=clientExceptionWithBody.getHttpStatus();
        errorDTO = new ErrorDTO(clientExceptionWithBody.getCode(),  error.getMessage());
      }
      else {
        httpStatus=HttpStatus.INTERNAL_SERVER_ERROR;
        errorDTO = defaultErrorDTO;
      }
      return ResponseEntity.status(httpStatus)
              .contentType(MediaType.APPLICATION_JSON)
              .body(errorDTO);
    }
  }

  /**
   * Logs client exceptions with appropriate detail based on their type and properties.
   *
   * @param error the RuntimeException to log
   * @param request the current HTTP request
   */
  public static void logClientException(RuntimeException error, ServerHttpRequest request) {
    Throwable unwrappedException = error.getCause() instanceof ServiceException
            ? error.getCause()
            : error;

    String clientExceptionMessage = "";
    if(error instanceof ClientException clientException) {
      clientExceptionMessage = ": HttpStatus %s - %s%s".formatted(
              clientException.getHttpStatus(),
              (clientException instanceof ClientExceptionWithBody clientExceptionWithBody) ? clientExceptionWithBody.getCode() + ": " : "",
              clientException.getMessage()
      );
    }

    if(!(error instanceof ClientException clientException) || clientException.isPrintStackTrace() || unwrappedException.getCause() != null){
      log.error("Something went wrong handling request {}{}", getRequestDetails(request), clientExceptionMessage, unwrappedException);
    } else {
      log.info("A {} occurred handling request {}{} at {}",
              unwrappedException.getClass().getSimpleName() ,
              getRequestDetails(request),
              clientExceptionMessage,
              unwrappedException.getStackTrace().length > 0 ? unwrappedException.getStackTrace()[0] : "UNKNOWN");
    }
  }

  public static String getRequestDetails(ServerHttpRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getURI());
  }

  /**
   * Handles {@link UncategorizedMongoDbException} to manage database-specific errors.
   * <p>
   * This method implements a specific mapping for Azure Cosmos DB (via MongoDB API):
   * <ul>
   *   <li><b>Error 16500 (RequestRateTooLarge):</b> Mapped to {@code HTTP 429 Too Many Requests}. 
   *       This occurs when the Request Units (RU) allocated to the collection are exceeded.</li>
   *   <li><b>Other MongoDB Errors:</b> Mapped to {@code HTTP 500 Internal Server Error} 
   *       using the default error DTO, after logging the full stack trace.</li>
   * </ul>
   * </p>
   *
   * @param error the exception thrown during MongoDB operations
   * @param request the current {@link ServerHttpRequest} for context
   * @return a {@link ResponseEntity} containing an {@link ErrorDTO} with:
   *         <ul>
   *           <li>Status 429 and "TOO_MANY_REQUESTS" code for throttling events</li>
   *           <li>Status 500 and a generic error message for other DB failures</li>
   *         </ul>
   */
  @ExceptionHandler(UncategorizedMongoDbException.class)
  protected ResponseEntity<ErrorDTO> handleMongoException(UncategorizedMongoDbException error, ServerHttpRequest request) {
      
      if (error.getCause() instanceof MongoCommandException mongoEx && mongoEx.getErrorCode() == 16500) {
          log.warn("CosmosDB throttling detected for request {}", request.getURI()); 
          return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS) // Status 429
                  .contentType(MediaType.APPLICATION_JSON)
                  .body(new ErrorDTO("TOO_MANY_REQUESTS", "CosmosDB Request Rate too large. Please retry later."));
      }
      logClientException(error, request);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(defaultErrorDTO);
  }
}

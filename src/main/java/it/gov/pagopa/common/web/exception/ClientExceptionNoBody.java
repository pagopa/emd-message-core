package it.gov.pagopa.common.web.exception;

import org.springframework.http.HttpStatus;

public class ClientExceptionNoBody extends ClientException{

  public ClientExceptionNoBody(HttpStatus httpStatus, String message) {
    super(httpStatus, message);
  }

}


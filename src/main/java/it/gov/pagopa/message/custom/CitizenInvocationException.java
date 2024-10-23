package it.gov.pagopa.message.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionMessage;

public class CitizenInvocationException extends ServiceException {

  public CitizenInvocationException() {
    this(ExceptionMessage.GENERI_ERROR,true,null);
  }

  public CitizenInvocationException(String message, boolean printStackTrace, Throwable ex) {
    this(ExceptionMessage.GENERI_ERROR, message, printStackTrace, ex);
  }
  public CitizenInvocationException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message,null, printStackTrace, ex);
  }
}

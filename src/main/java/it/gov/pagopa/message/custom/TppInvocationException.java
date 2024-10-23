package it.gov.pagopa.message.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.message.constants.MessageCoreConstants;

public class TppInvocationException extends ServiceException {
  public TppInvocationException() {
    this(MessageCoreConstants.ExceptionMessage.GENERI_ERROR,true,null);
  }

  public TppInvocationException(String message, boolean printStackTrace, Throwable ex) {
    this(MessageCoreConstants.ExceptionCode.GENERI_ERROR, message, printStackTrace, ex);
  }
  public TppInvocationException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message,null, printStackTrace, ex);
  }
}

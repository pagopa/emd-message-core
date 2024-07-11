package it.gov.pagopa.message.core.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.message.core.constants.MessageCoreConstants.ExceptionCode;

public class EmdEncryptionException extends ServiceException {

  public EmdEncryptionException(String message, boolean printStackTrace, Throwable ex) {
    this(ExceptionCode.GENERI_ERROR, message, printStackTrace, ex);
  }
  public EmdEncryptionException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message,null, printStackTrace, ex);
  }
}

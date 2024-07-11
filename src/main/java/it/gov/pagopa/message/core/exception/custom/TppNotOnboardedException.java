package it.gov.pagopa.message.core.exception.custom;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.message.core.constants.MessageCoreConstants.ExceptionCode;

public class TppNotOnboardedException extends ServiceException {

  public TppNotOnboardedException(String message, boolean printStackTrace, Throwable ex) {
    this(ExceptionCode.TPP_NOT_ONBOARDED, message,printStackTrace,ex);
  }

  public TppNotOnboardedException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message,null, printStackTrace, ex);
  }
}

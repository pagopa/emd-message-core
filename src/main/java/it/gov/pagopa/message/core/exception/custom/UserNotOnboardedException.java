package it.gov.pagopa.message.core.exception.custom;


import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.message.core.constants.MessageCoreConstants.ExceptionCode;


public class UserNotOnboardedException extends ServiceException {

  public UserNotOnboardedException(String message, boolean printStackTrace, Throwable ex) {
    this(ExceptionCode.USER_NOT_ONBOARDED, message,printStackTrace,ex);
  }


  public UserNotOnboardedException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message,null, printStackTrace, ex);
  }
}

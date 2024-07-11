package it.gov.pagopa.message.core.configuration;


import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.message.core.exception.custom.EmdEncryptionException;
import it.gov.pagopa.message.core.exception.custom.TppNotOnboardedException;
import it.gov.pagopa.message.core.exception.custom.UserNotOnboardedException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ServiceExceptionConfiguration {

  @Bean
  public Map<Class<? extends ServiceException>, HttpStatus> serviceExceptionMapper() {
    Map<Class<? extends ServiceException>, HttpStatus> exceptionMap = new HashMap<>();

    // BadRequest


    // Forbidden



    // NotFound
    exceptionMap.put(TppNotOnboardedException.class, HttpStatus.NOT_FOUND);
    exceptionMap.put(UserNotOnboardedException.class, HttpStatus.NOT_FOUND);

    // InternalServerError
    exceptionMap.put(EmdEncryptionException.class, HttpStatus.INTERNAL_SERVER_ERROR);

    // TooManyRequests

    return exceptionMap;
  }

}

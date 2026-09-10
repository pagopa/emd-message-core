package it.gov.pagopa.common.configuration;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.message.config.ExceptionMap;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionCode;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ExceptionMapTest {

    private ExceptionMap exceptionMap;

    @BeforeEach
    void setUp() {
        exceptionMap = new ExceptionMap();
    }

    @Test
    void throwException_KnownKey_MESSAGE_NOT_FOUND() {
        String customMessage = "Il messaggio richiesto non esiste";

        RuntimeException result = exceptionMap.throwException(ExceptionName.MESSAGE_NOT_FOUND, customMessage);

        Assertions.assertNotNull(result, "L'eccezione non deve essere null");
        
        Assertions.assertInstanceOf(ClientExceptionWithBody.class, result);
        
        ClientExceptionWithBody exception = (ClientExceptionWithBody) result;
        
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        Assertions.assertEquals(ExceptionCode.MESSAGE_NOT_FOUND, exception.getCode());
        Assertions.assertEquals(customMessage, exception.getMessage());
    }

    @Test
    void throwException_UnknownKey_ReturnsGenericRuntimeException() {
        String unknownKey = "CHIAVE_NON_MAPPATA";
        String customMessage = "Un messaggio d'errore";

        RuntimeException result = exceptionMap.throwException(unknownKey, customMessage);

        Assertions.assertNotNull(result, "L'eccezione non deve essere null");
        
        Assertions.assertEquals(RuntimeException.class, result.getClass());
        
        Assertions.assertNull(result.getMessage(), "Il messaggio dell'eccezione generica dovrebbe essere null");
    }
}
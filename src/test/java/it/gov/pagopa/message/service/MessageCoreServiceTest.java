package it.gov.pagopa.message.service;

import it.gov.pagopa.message.connector.CitizenConnectorImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import static it.gov.pagopa.message.utils.TestUtils.FISCAL_CODE;
import static it.gov.pagopa.message.utils.TestUtils.MESSAGE_DTO;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        MessageCoreServiceImpl.class,
})
class MessageCoreServiceTest {
    @MockBean
    MessageProducerServiceImpl messageProducerService;
    @MockBean
    CitizenConnectorImpl citizenConnector;

    @Autowired
    MessageCoreServiceImpl messageCoreService;


    @Test
    void sendMessage_Ok()  {
        when(citizenConnector.checkFiscalCode(FISCAL_CODE)).thenReturn(Mono.just("OK"));
        when(messageProducerService.enqueueMessage(MESSAGE_DTO)).thenReturn(Mono.empty());

        Boolean result = messageCoreService.send(MESSAGE_DTO).block();
        Assertions.assertEquals(true, result);

    }

    @Test
    void sendMessage_Ko()  {
        when(citizenConnector.checkFiscalCode(FISCAL_CODE)).thenReturn(Mono.just("NO CHANNEL ENABLED"));

        Boolean result = messageCoreService.send(MESSAGE_DTO).block();
        Assertions.assertEquals(false,result);
    }

}

package it.gov.pagopa.message.service;

import it.gov.pagopa.message.config.ExceptionMap;
import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import it.gov.pagopa.message.dto.ResponseMessageMapperObjectToDTO;
import it.gov.pagopa.message.repository.MessageRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.gov.pagopa.message.utils.TestUtils.*;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        MessageCoreServiceImpl.class,
})
class MessageCoreServiceTest {
    @MockitoBean
    MessageProducerServiceImpl messageProducerService;
    @MockitoBean
    CitizenConnectorImpl citizenConnector;
    @MockitoBean
    MessageRepository messageRepository;
    @MockitoBean
    ResponseMessageMapperObjectToDTO messageMapperObjectToDTO;
    @MockitoBean
    ExceptionMap exceptionMap;

    @Autowired
    MessageCoreServiceImpl messageCoreService;


    @Test
    void sendMessage_Ok()  {
        when(citizenConnector.checkFiscalCode(FISCAL_CODE)).thenReturn(Mono.just("OK"));
        when(messageProducerService.enqueueMessage(MESSAGE_DTO,MESSAGE_ID)).thenReturn(Mono.empty());
        StepVerifier.create(messageCoreService.send(MESSAGE_DTO))
                .expectNext(true)
                .verifyComplete();

    }

    @Test
    void sendMessage_Ko()  {
        when(citizenConnector.checkFiscalCode(FISCAL_CODE)).thenReturn(Mono.just("NO CHANNEL ENABLED"));

            StepVerifier.create(messageCoreService.send(MESSAGE_DTO))
                .expectNext(false)
                .verifyComplete();
    }

}

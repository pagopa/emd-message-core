package it.gov.pagopa.message.service;

import it.gov.pagopa.message.config.ExceptionMap;
import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import it.gov.pagopa.message.dto.ResponseMessageDTO;
import it.gov.pagopa.message.dto.ResponseMessageMapperObjectToDTO;
import it.gov.pagopa.message.model.Message;
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
import static org.mockito.ArgumentMatchers.*;
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

    @Test
    void getMessage_Ok() {
        String testMessageId = "test-message-id";
        
        Message mockMessage = new Message();
        mockMessage.setMessageId(testMessageId);
        mockMessage.setRecipientId("test-recipient");

        ResponseMessageDTO expectedResponse = ResponseMessageDTO.builder()
                .messageId(testMessageId)
                .recipientId("test-recipient")
                .build();

        when(messageRepository.findByMessageId(testMessageId)).thenReturn(Mono.just(mockMessage));
        when(messageMapperObjectToDTO.map(mockMessage)).thenReturn(expectedResponse);

        StepVerifier.create(messageCoreService.getMessage(testMessageId))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void getMessage_NotFound() {
        String testMessageId = "not-found-message-id";
        
        RuntimeException mockException = new RuntimeException("Test Exception Not Found");

        when(messageRepository.findByMessageId(testMessageId)).thenReturn(Mono.empty());
        
        when(exceptionMap.throwException(any(), any())).thenReturn(mockException);

        StepVerifier.create(messageCoreService.getMessage(testMessageId))
                .expectErrorMatches(throwable -> throwable.equals(mockException))
                .verify();
    }

}

package it.gov.pagopa.message.service;

import it.gov.pagopa.message.config.ExceptionMap;
import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionMessage;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionName;
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

    @Autowired
    MessageCoreServiceImpl messageCoreService;

    @MockitoBean
    MessageRepository messageRepository;

    @MockitoBean
    ExceptionMap exceptionMap;


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
    void deleteMessage_Ok() {
        String entityId = "entity-123";
        String messageId = "msg-123";

        when(messageRepository.deleteByEntityIdAndMessageId(entityId, messageId))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(messageCoreService.deleteMessage(entityId, messageId))
                .verifyComplete();
    }

    @Test
    void deleteMessage_Ko_NotFound() {
        String entityId = "entity-123";
        String messageId = "msg-123";

        when(messageRepository.deleteByEntityIdAndMessageId(entityId, messageId))
                .thenReturn(Mono.just(0L));

        RuntimeException expectedException = new RuntimeException("Message not found custom error");
        when(exceptionMap.throwException(ExceptionName.MESSAGE_NOT_FOUND, ExceptionMessage.MESSAGE_NOT_FOUND))
                .thenReturn(expectedException);

        StepVerifier.create(messageCoreService.deleteMessage(entityId, messageId))
                .expectErrorMatches(throwable -> throwable.equals(expectedException))
                .verify();
    }

    @Test
    void deleteMessage_Ko_DbError() {
        String entityId = "entity-123";
        String messageId = "msg-123";

        RuntimeException dbError = new RuntimeException("DB Connection Timeout");
        when(messageRepository.deleteByEntityIdAndMessageId(entityId, messageId))
                .thenReturn(Mono.error(dbError));

        StepVerifier.create(messageCoreService.deleteMessage(entityId, messageId))
                .expectErrorMatches(throwable -> throwable.equals(dbError))
                .verify();
    }

}

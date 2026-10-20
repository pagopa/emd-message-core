package it.gov.pagopa.message.service;

import it.gov.pagopa.message.config.ExceptionMap;
import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import it.gov.pagopa.message.dto.ResponseMessageMapperObjectToDTO;
import it.gov.pagopa.message.enums.MessageState;
import it.gov.pagopa.message.dto.MessageCursorCodec;
import it.gov.pagopa.message.dto.MessageKeysetPage;
import it.gov.pagopa.message.dto.MessageSearchCursor;
import it.gov.pagopa.message.dto.MessageSearchParameter;
import it.gov.pagopa.message.dto.MessageSearchResponseDTO;
import it.gov.pagopa.message.dto.ResponseMessageDTO;
import it.gov.pagopa.message.model.Message;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionMessage;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionName;
import it.gov.pagopa.message.repository.MessageRepository;

import org.junit.jupiter.api.Assertions;
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
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

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
    ResponseMessageMapperObjectToDTO messageMapperObjectToDTO;

    @Autowired
    MessageCoreServiceImpl messageCoreService;

    @MockitoBean
    MessageRepository messageRepository;

    @MockitoBean
    ExceptionMap exceptionMap;

    @MockitoBean
    private MessageCursorCodec cursorCodec;

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

    /**
     * Test case for successful message search.
     * <p>
     * <b>Scenario:</b> A search is performed with valid filters. The repository returns a list 
     * of messages and a total count.
     * <b>Expected:</b> The service returns a {@link MessageSearchResponseDTO} containing 
     * the mapped DTOs and correct pagination metadata.
     */
    @Test
    void searchMessages_Ok() {

        // Given
        String messageId = "MSG_ID";
        String recipientId = "RECIPIENT_ID";
        String originId = "ORIGIN_ID";
        MessageState messageState = MessageState.ERROR;
        String cursor = "CURSOR";
        LocalDateTime now = LocalDateTime.now();
        int size = 10;
        List<String> fields = List.of("messageId", "recipientId");

        MessageSearchParameter params = new MessageSearchParameter();
        params.setMessageId(messageId);
        params.setRecipientId(recipientId);
        params.setOriginId(originId);
        params.setState(messageState);
        params.setStartDate(now);
        params.setEndDate(now);
        params.setCursor(cursor);
        params.setSize(size);
        params.setFields(fields);

        Message messageMock = new Message();
        messageMock.setMessageId(messageId);
        messageMock.setId("66f2a1b2c3d4e5f678901234");
        messageMock.setMessageRegistrationDate("2026-09-28T10:00:00");

        ResponseMessageDTO messageDtoMock = ResponseMessageDTO.builder()
                                                .messageId(messageId)
                                                .build();

        /*
        * Il service riceve il cursor come String e lo decodifica
        * prima di chiamare il repository.
        */
        MessageSearchCursor decodedCursor = new MessageSearchCursor("2026-09-28T09:59:00", "66f2a1b2c3d4e5f678901233");

        when(cursorCodec.decode(cursor))
                .thenReturn(decodedCursor);

        /*
        * Il repository ora restituisce una MessageKeysetPage.
        */
        MessageKeysetPage<Message> repositoryResult = new MessageKeysetPage<>(List.of(messageMock), true, "NEXT_CURSOR");

        when(messageRepository.searchMessages(
                eq(params),
                eq(decodedCursor),
                anyInt(),
                anySet()
        )).thenReturn(Mono.just(repositoryResult));

        /*
        * Mock del count.
        */
        when(messageRepository.countMessages(eq(params)))
            .thenReturn(Mono.just(1L));
        /*
        * Mock del mapper.
        */
        when(messageMapperObjectToDTO.map(any(), anySet()))
            .thenReturn(messageDtoMock);

        // When & Then
        StepVerifier.create(messageCoreService.searchMessages(params))
        .assertNext(response -> {
            // Utilizzo di Assertions. per evitare ambiguità JUnit 4/5
            Assertions.assertNotNull(response);
            
            // Content
            Assertions.assertEquals(1, response.getContent().size());
            Assertions.assertEquals(messageId, response.getContent().get(0).getMessageId());
            
            // Count
            Assertions.assertEquals(1L, response.getTotalElements());
            
            // Pagination
            Assertions.assertEquals(10, response.getSize());
            Assertions.assertTrue(response.isHasNext());
            Assertions.assertEquals("NEXT_CURSOR", response.getNextCursor());
            
            Assertions.assertEquals(1, response.getTotalPages());
        })
        .verifyComplete();
    }

    /**
     * Test case for search with no results.
     * <p>
     * <b>Scenario:</b> No messages match the provided search criteria.
     * <b>Expected:</b> The service returns an empty content list and total elements equal to zero.
     */
    @Test
    void searchMessages_EmptyResult_Ok() {
        // Given
        MessageSearchParameter params = new MessageSearchParameter();
        params.setSize(10);
        when(messageRepository.searchMessages(any(), any(), anyInt(), anySet()))
            .thenReturn(Mono.just(new MessageKeysetPage<>(List.of(), false, null)));

        when(messageRepository.countMessages(any()))
            .thenReturn(Mono.just(0L));

        // When & Then
        StepVerifier.create(messageCoreService.searchMessages(params))
            .assertNext(response -> {
                // Utilizzo di Assertions. per evitare l'errore ambiguous JUnit 4/5
                Assertions.assertTrue(response.getContent().isEmpty(), "La lista dei risultati dovrebbe essere vuota");
                Assertions.assertEquals(0L, response.getTotalElements(), "Il totale degli elementi dovrebbe essere 0");
                Assertions.assertEquals(0, response.getTotalPages(), "Il totale delle pagine dovrebbe essere 0");
                Assertions.assertFalse(response.isHasNext(), "hasNext dovrebbe essere false");
            })
            .verifyComplete();

        // Verifichiamo che il repository sia stato effettivamente chiamato con l'oggetto params
        verify(messageRepository).searchMessages(eq(params), any(), anyInt(), anySet());
        verify(messageRepository).countMessages(eq(params));
    }

    /**
     * Test case for pagination capping logic.
     * <p>
     * <b>Scenario:</b> The client requests a page size (500) that exceeds the maximum allowed (100).
     * <b>Expected:</b> The service caps the size to the configured maximum before calling the repository.
     */
    @Test
    void searchMessages_CappedPagination_Ok() {
        int requestedSize = 500;
        int maxSize = 100;

        MessageSearchParameter params = new MessageSearchParameter();
        params.setSize(requestedSize);

        when(messageRepository.searchMessages(any(), any(), eq(maxSize), anySet()))
            .thenReturn(Mono.just(new MessageKeysetPage<>(List.of(), false, null)));
        
            when(messageRepository.countMessages(any()))
            .thenReturn(Mono.just(0L));

        StepVerifier.create(messageCoreService.searchMessages(params))
            .assertNext(response -> {
                // Verifichiamo che la risposta indichi la size effettivamente utilizzata (quella cappata)
                Assertions.assertEquals(maxSize, response.getSize(), "La size nella risposta dovrebbe essere quella cappata");
            })
            .verifyComplete();

        // Verifica finale: conferma che il Service abbia invocato il repository con 100 e non con 500
        verify(messageRepository).searchMessages(any(), any(), eq(maxSize), anySet());
        
        // Verifichiamo anche che countMessages sia stato chiamato
        verify(messageRepository).countMessages(any());
    }


    @Test
    void getMessage_Ok() {
        String testEntityId = "test-entity-id";
        String testMessageId = "test-message-id";
        
        Message mockMessage = new Message();
        mockMessage.setMessageId(testMessageId);
        mockMessage.setEntityId(testEntityId);
        mockMessage.setRecipientId("test-recipient");

        ResponseMessageDTO expectedResponse = ResponseMessageDTO.builder()
                .entityId(testEntityId)
                .messageId(testMessageId)
                .recipientId("test-recipient")
                .build();

        when(messageRepository.findByEntityIdAndMessageId(testEntityId, testMessageId)).thenReturn(Mono.just(mockMessage));
        when(messageMapperObjectToDTO.map(mockMessage)).thenReturn(expectedResponse);

        StepVerifier.create(messageCoreService.getMessage(testEntityId, testMessageId))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void getMessage_NotFound() {
        String testEntityId = "test-entity-id";
        String testMessageId = "not-found-message-id";
        
        RuntimeException mockException = new RuntimeException("Test Exception Not Found");

        when(messageRepository.findByEntityIdAndMessageId(testEntityId, testMessageId)).thenReturn(Mono.empty());
        
        when(exceptionMap.throwException(any(), any())).thenReturn(mockException);

        StepVerifier.create(messageCoreService.getMessage(testEntityId, testMessageId))
                .expectErrorMatches(throwable -> throwable.equals(mockException))
                .verify();
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

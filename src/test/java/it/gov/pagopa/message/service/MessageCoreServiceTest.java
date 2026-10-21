package it.gov.pagopa.message.service;

import it.gov.pagopa.message.config.ExceptionMap;
import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import it.gov.pagopa.message.dto.ResponseMessageMapperObjectToDTO;
import it.gov.pagopa.message.dto.MessageCursorCodec;
import it.gov.pagopa.message.dto.MessageKeysetPage;
import it.gov.pagopa.message.dto.MessageSearchCursor;
import it.gov.pagopa.message.dto.MessageSearchResponseDTO;
import it.gov.pagopa.message.dto.ResponseMessageDTO;
import it.gov.pagopa.message.model.Message;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionMessage;
import it.gov.pagopa.message.constants.MessageCoreConstants.ExceptionName;
import it.gov.pagopa.message.repository.MessageRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.gov.pagopa.message.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

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
        String cursor = "CURSOR";
        LocalDateTime now = LocalDateTime.now();
        int size = 10;

        List<String> fields = List.of("messageId", "recipientId");

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
        MessageKeysetPage<Message> repositoryResult =
                new MessageKeysetPage<>(
                        List.of(messageMock),
                        true,
                        "NEXT_CURSOR"
                );

        when(messageRepository.searchMessages(
                eq(messageId),
                eq(recipientId),
                eq(originId),
                eq(now),
                eq(now),
                eq(decodedCursor),
                eq(size),
                anySet()
        )).thenReturn(Mono.just(repositoryResult));

        /*
        * Mock del count.
        */
        when(messageRepository.countMessages(messageId, recipientId, originId, now, now ))
            .thenReturn( Mono.just(1L));

        /*
        * Mock del mapper.
        */
        when(messageMapperObjectToDTO.map(any(), anySet()))
            .thenReturn(messageDtoMock);

        // When & Then
        StepVerifier.create(
                messageCoreService.searchMessages(
                        messageId,
                        recipientId,
                        originId,
                        now,
                        now,
                        cursor,
                        size,
                        fields
                )
        )
        .assertNext(response -> {

            assertNotNull(response);
            // Content
            assertEquals( 1,response.getContent().size());
            assertEquals(messageId,
                    response.getContent()
                            .get(0)
                            .getMessageId()
            );

            // Count
            assertEquals(1L,response.getTotalElements());
            // Pagination
            assertEquals(size,response.getSize());
            assertTrue(response.isHasNext());
            assertEquals("NEXT_CURSOR", response.getNextCursor());
            // 1 elemento / 10 per pagina = 1 pagina
            assertEquals(1, response.getTotalPages());
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
        when(messageRepository.searchMessages(
                any(), any(), any(), any(), any(), any(), anyInt(), anySet()))
            .thenReturn(Mono.just(new MessageKeysetPage<>(List.of(), false, null)));
        when(messageRepository.countMessages(
                any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(0L));

        // When & Then
        StepVerifier.create( messageCoreService.searchMessages(
                        null, null, null, null, null, null, 10, null))
            .assertNext(response -> {
                Assertions.assertTrue(response.getContent().isEmpty());
                Assertions.assertEquals(0,response.getTotalElements() );
            })
            .verifyComplete();
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

        when(messageRepository.searchMessages(any(), any(), any(), any(), any(), any(),ArgumentMatchers.eq(maxSize), anySet()))
            .thenReturn(Mono.just(new MessageKeysetPage<>(List.of(), false,null)));
        when(messageRepository.countMessages(
                any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(0L));

        StepVerifier.create(
                messageCoreService.searchMessages(
                        null, null, null, null, null, null,
                        requestedSize,
                        null))
            .expectNextCount(1)
            .verifyComplete();

        // Verifica che al repository sia arrivato 100 invece di 500
        Mockito.verify(messageRepository).searchMessages(any(), any(), any(), any(), any(),any(),ArgumentMatchers.eq(maxSize),anySet());
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

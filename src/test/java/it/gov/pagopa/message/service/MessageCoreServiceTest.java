package it.gov.pagopa.message.service;

import it.gov.pagopa.message.config.ExceptionMap;
import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import it.gov.pagopa.message.dto.MessageMapperObjectToDTO;
import it.gov.pagopa.message.dto.MessageSearchResponseDTO;
import it.gov.pagopa.message.model.Message;
import it.gov.pagopa.message.repository.MessageRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.gov.pagopa.message.utils.TestUtils.*;
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
    MessageMapperObjectToDTO messageMapperObjectToDTO;
    @MockitoBean MessageRepository messageRepository;
    @MockitoBean ExceptionMap exceptionMap;

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
        LocalDateTime now = LocalDateTime.now();
        int page = 0;
        int size = 10;
        List<String> fields = List.of("messageId", "recipientId");

        Message messageMock = new Message();
        messageMock.setMessageId(messageId);

        it.gov.pagopa.message.dto.MessageDTO messageDtoMock = it.gov.pagopa.message.dto.MessageDTO.builder()
                .messageId(messageId)
                .build();

        // Mock Repository: search restituisce un Flux con un elemento
        when(messageRepository.searchMessages(
                org.mockito.ArgumentMatchers.eq(messageId),
                org.mockito.ArgumentMatchers.eq(recipientId),
                org.mockito.ArgumentMatchers.eq(originId),
                org.mockito.ArgumentMatchers.eq(now),
                org.mockito.ArgumentMatchers.eq(now),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anySet()))
            .thenReturn(reactor.core.publisher.Flux.just(messageMock));

        // Mock Repository: count restituisce 1
        when(messageRepository.countMessages(messageId, recipientId, originId, now, now))
            .thenReturn(Mono.just(1L));

        // Mock Mapper
        when(messageMapperObjectToDTO.map(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anySet()))
            .thenReturn(messageDtoMock);

        // When & Then
        StepVerifier.create(messageCoreService.searchMessages(messageId, recipientId, originId, now, now, page, size, fields))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertNotNull(response);
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.getContent().size());
                    org.junit.jupiter.api.Assertions.assertEquals(1L, response.getTotalElements());
                    org.junit.jupiter.api.Assertions.assertEquals(0, response.getPage());
                    org.junit.jupiter.api.Assertions.assertEquals(size, response.getSize());
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
        when(messageRepository.searchMessages(any(), any(), any(), any(), any(), anyInt(), anyInt(), anySet()))
            .thenReturn(reactor.core.publisher.Flux.empty());
        when(messageRepository.countMessages(any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(0L));

        // When & Then
        StepVerifier.create(messageCoreService.searchMessages(null, null, null, null, null, 0, 10, null))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertTrue(response.getContent().isEmpty());
                    org.junit.jupiter.api.Assertions.assertEquals(0, response.getTotalElements());
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
        int maxSize = 100; // Assumed maxPageSize configuration

        when(messageRepository.searchMessages(any(), any(), any(), any(), any(), anyInt(), ArgumentMatchers.eq(maxSize), anySet()))
            .thenReturn(Flux.empty());
        when(messageRepository.countMessages(any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(0L));

        StepVerifier.create(messageCoreService.searchMessages(null, null, null, null, null, 0, requestedSize, null))
                .expectNextCount(1)
                .verifyComplete();

        // Verifica che al repository sia arrivato 100 invece di 500
        org.mockito.Mockito.verify(messageRepository).searchMessages(any(), any(), any(), any(), any(), anyInt(), ArgumentMatchers.eq(maxSize), anySet());
    }

}

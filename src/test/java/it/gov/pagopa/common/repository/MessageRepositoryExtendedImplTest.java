package it.gov.pagopa.common.repository;

import it.gov.pagopa.message.model.Message;
import it.gov.pagopa.message.repository.MessageRepositoryExtendedImpl;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageRepositoryExtendedImplTest {

    @Mock
    private ReactiveMongoTemplate reactiveMongoTemplate;

    private MessageRepositoryExtendedImpl messageRepository;

    @BeforeEach
    void setUp() {
        messageRepository = new MessageRepositoryExtendedImpl(reactiveMongoTemplate);
    }

    @Test
    void searchMessages_AllFilters_Ok() {
        // Given
        String messageId = "MSG123";
        String recipientId = "RECIPIENT123";
        String originId = "ORIGIN123";
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        int page = 0;
        int size = 10;

        Set<String> fields = Set.of("content", "title");

        when(reactiveMongoTemplate.find(any(Query.class), eq(Message.class)))
                .thenReturn(Flux.just(new Message()));

        // When
        Flux<Message> result = messageRepository.searchMessages(
                messageId,
                recipientId,
                originId,
                startDate,
                endDate,
                page,
                size,
                fields
        );

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        ArgumentCaptor<Query> queryCaptor =
                ArgumentCaptor.forClass(Query.class);

        verify(reactiveMongoTemplate)
                .find(queryCaptor.capture(), eq(Message.class));

        Query capturedQuery = queryCaptor.getValue();
        Document queryObject = capturedQuery.getQueryObject();

        // =========================
        // Verifica struttura query
        // =========================
        assertTrue(
                queryObject.containsKey("$and"),
                "Deve contenere un operatore $and"
        );

        List<Document> andConditions =
                (List<Document>) queryObject.get("$and");

        assertNotNull(andConditions);
        assertEquals(4, andConditions.size());

        // =========================
        // Verifica messageId
        // =========================
        assertTrue(andConditions.stream()
                .anyMatch(condition ->
                        messageId.equals(
                                condition.getString("messageId")
                        )
                )
        );

        // =========================
        // Verifica recipientId
        // =========================
        assertTrue(andConditions.stream()
                .anyMatch(condition ->
                        recipientId.equals(
                                condition.getString("recipientId")
                        )
                )
        );

        // =========================
        // Verifica originId
        // =========================
        assertTrue(andConditions.stream()
                .anyMatch(condition ->
                        originId.equals(
                                condition.getString("originId")
                        )
                )
        );

        // =========================
        // Verifica date
        // =========================
        Document dateCondition = andConditions.stream()
                .filter(condition ->
                        condition.containsKey("messageRegistrationDate")
                )
                .findFirst()
                .orElseThrow();

        Document dateDocument =
                (Document) dateCondition.get("messageRegistrationDate");

        assertEquals(
                startDate.toString(),
                dateDocument.get("$gte")
        );

        assertEquals(
                endDate.toString(),
                dateDocument.get("$lte")
        );

        // =========================
        // Verifica paginazione
        // =========================
        assertEquals(size, capturedQuery.getLimit());
        assertEquals(0, capturedQuery.getSkip());

        // =========================
        // Verifica projection
        // =========================
        Document fieldsObject = capturedQuery.getFieldsObject();

        assertEquals(1, fieldsObject.get("content"));
        assertEquals(1, fieldsObject.get("title"));
        assertEquals(1, fieldsObject.get("messageId"));
    }

    @Test
    void searchMessages_OnlyDates_Ok() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 10, 0);
        
        when(reactiveMongoTemplate.find(any(Query.class), eq(Message.class)))
                .thenReturn(Flux.empty());

        // When
        messageRepository.searchMessages(null, null, null, startDate, null, 0, 10, null).subscribe();

        // Then
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(reactiveMongoTemplate).find(queryCaptor.capture(), eq(Message.class));

        Document queryObject = queryCaptor.getValue().getQueryObject();
        // Verifichiamo che ci sia il filtro gte sulla data
        String queryStr = queryObject.toJson();
        assertTrue(queryStr.contains("$gte"));
        assertTrue(queryStr.contains("2023-01-01T10:00"));
    }

    @Test
    void countMessages_Ok() {
        // Given
        String messageId = "MSG123";
        when(reactiveMongoTemplate.count(any(Query.class), eq(Message.class)))
                .thenReturn(Mono.just(1L));

        // When
        Mono<Long> result = messageRepository.countMessages(messageId, null, null, null, null);

        // Then
        StepVerifier.create(result)
                .expectNext(1L)
                .verifyComplete();

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(reactiveMongoTemplate).count(queryCaptor.capture(), eq(Message.class));

        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(queryObject.containsKey("messageId") || queryObject.containsKey("$and"));
    }

    @Test
    void searchMessages_NoFilters_EmptyQuery() {
        // Given
        when(reactiveMongoTemplate.find(any(Query.class), eq(Message.class)))
                .thenReturn(Flux.empty());

        // When
        messageRepository.searchMessages(null, null, null, null, null, 0, 20, null).subscribe();

        // Then
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(reactiveMongoTemplate).find(queryCaptor.capture(), eq(Message.class));

        Query capturedQuery = queryCaptor.getValue();
        assertTrue(capturedQuery.getQueryObject().isEmpty(), "La query non dovrebbe avere filtri");
        assertEquals(20, capturedQuery.getLimit());
    }
}
package it.gov.pagopa.common.repository;

import it.gov.pagopa.message.dto.MessageCursorCodec;
import it.gov.pagopa.message.dto.MessageKeysetPage;
import it.gov.pagopa.message.dto.MessageSearchCursor;
import it.gov.pagopa.message.model.Message;
import it.gov.pagopa.message.repository.MessageRepositoryExtendedImpl;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageRepositoryExtendedImplTest {

    @Mock
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Mock
    private MessageCursorCodec cursorCodec;

    private MessageRepositoryExtendedImpl messageRepository;

    private static final String RU_COMMAND = "{getLastRequestStatistics: 1}";

    @BeforeEach
    void setUp() {
        messageRepository = new MessageRepositoryExtendedImpl(reactiveMongoTemplate, cursorCodec);

        lenient().when(reactiveMongoTemplate.executeCommand(eq(RU_COMMAND)))
                .thenReturn(Mono.just(new Document("RequestCharge", 10.0)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchMessages_AllFilters_Ok() {

        // Given
        String messageId = "MSG123";
        String recipientId = "RECIPIENT123";
        String originId = "ORIGIN123";
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        int size = 10;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        Set<String> fields = Set.of("content", "title");
        String cursorId = "66f2a1b2c3d4e5f678901234";
        String cursorDate = "2026-09-28T10:00:00";
        String FIELD_MESSAGE_ID = "messageId";
        String FIELD_RECIPIENT_ID = "recipientId";
        String FIELD_ORIGIN_ID = "originId";
        String FIELD_REGISTRATION_DATE = "messageRegistrationDate";
        MessageSearchCursor cursor = new MessageSearchCursor( cursorDate,cursorId);
        Message message = new Message();

        /*
        * Il risultato contiene un solo documento.
        * Con size=10 non ci sarà una pagina successiva.
        */
        when(reactiveMongoTemplate.find( any(Query.class), eq(Message.class) )).thenReturn( Flux.just(message));

        /*
        * Il repository ora legge le RU tramite
        * getLastRequestStatistics.
        */
        Document statistics = new Document("RequestCharge", 2.5);

        when(reactiveMongoTemplate.executeCommand(anyString())).thenReturn( Mono.just(statistics));

        // When
        Mono<MessageKeysetPage<Message>> result = messageRepository.searchMessages(messageId, recipientId, originId, startDate, endDate, cursor, size, fields);

        // Then
        StepVerifier.create(result)
                .assertNext(page -> {
                    assertEquals(1, page.content().size());
                    assertFalse(page.hasNext());
                    assertNull(page.nextCursor());
                })
                .verifyComplete();

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        verify(reactiveMongoTemplate).find(queryCaptor.capture(),eq(Message.class) );

        Query capturedQuery = queryCaptor.getValue();

        Document queryObject = capturedQuery.getQueryObject();

        // =========================================================
        // FILTRI PRINCIPALI
        // =========================================================

        assertEquals(messageId,queryObject.getString(FIELD_MESSAGE_ID));
        assertEquals(recipientId,queryObject.getString(FIELD_RECIPIENT_ID));

        assertEquals(originId, queryObject.getString(FIELD_ORIGIN_ID));

        // =========================================================
        // DATE
        // =========================================================

        Document dateCondition = (Document) queryObject.get(FIELD_REGISTRATION_DATE);

        assertNotNull(dateCondition);
        assertEquals(startDate.format(formatter), dateCondition.get("$gte"));
        assertEquals(endDate.format(formatter), dateCondition.get("$lte"));

        // ---------------------------------------------------------
        // KEYSET CURSOR
        // ---------------------------------------------------------

        assertTrue(queryObject.containsKey("$or"),"Deve contenere il criterio di keyset pagination");
        List<Document> cursorConditions =(List<Document>) queryObject.get("$or");
        assertEquals(2,cursorConditions.size());

        /*
        * Caso 1:
        *
        * messageRegistrationDate < cursorDate
        */
        Document dateCursorCondition = cursorConditions.get(0);
        Document lessThanDate = (Document) dateCursorCondition.get("messageRegistrationDate");

        assertEquals(cursorDate,lessThanDate.get("$lt"));

        /*
        * Caso 2:
        *
        * messageRegistrationDate == cursorDate
        * AND
        * _id < cursorId
        */
        Document sameDateAndId = cursorConditions.get(1);
        assertTrue(sameDateAndId.containsKey("$and"));

        List<Document> sameDateConditions = (List<Document>) sameDateAndId.get("$and");

        assertEquals(2, sameDateConditions.size());

        Document sameDate = sameDateConditions.stream()
                        .filter(condition ->condition.containsKey("messageRegistrationDate"))
                        .findFirst()
                        .orElseThrow();

        assertEquals(cursorDate, sameDate.getString("messageRegistrationDate"));

        Document idCondition = sameDateConditions.stream()
                        .filter(condition ->condition.containsKey("_id"))
                        .findFirst()
                        .orElseThrow();

        Document idDocument = (Document) idCondition.get("_id");
        assertEquals(new ObjectId(cursorId), idDocument.get("$lt"));

        // ---------------------------------------------------------
        // PAGINATION
        // ---------------------------------------------------------

        /*
        * Non viene più utilizzato skip().
        *
        * Viene richiesto size + 1 per capire se esiste
        * una pagina successiva.
        */
        assertEquals(size + 1, capturedQuery.getLimit());
        assertEquals(0, capturedQuery.getSkip());

        // ---------------------------------------------------------
        // SORT
        // ---------------------------------------------------------

        Document sortObject = capturedQuery.getSortObject();
        assertEquals(-1, sortObject.getInteger("messageRegistrationDate"));
        assertEquals(-1,sortObject.getInteger("_id"));

        // ---------------------------------------------------------
        // PROJECTION
        // ---------------------------------------------------------

        Document fieldsObject = capturedQuery.getFieldsObject();
        assertEquals(1, fieldsObject.get("content"));
        assertEquals(1, fieldsObject.get("title"));

        /*
        * Necessari per costruire il nextCursor.
        */
        assertEquals(1, fieldsObject.get("_id"));
        assertEquals(1, fieldsObject.get("messageRegistrationDate"));

        /*
        * messageId NON viene aggiunto automaticamente.
        */
        assertFalse(fieldsObject.containsKey("messageId"));
    }

    @Test
    void searchMessages_OnlyDates_Ok() {

        // Given
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 10, 0);

        int size = 10;

        when(reactiveMongoTemplate.find(any(Query.class),eq(Message.class))).thenReturn(Flux.empty());

        when(reactiveMongoTemplate.executeCommand(anyString()))
                .thenReturn(Mono.just(new Document("RequestCharge", 2.5)));

        // When
        StepVerifier.create(messageRepository.searchMessages(
                        null,
                        null,
                        null,
                        startDate,
                        null,
                        null,
                        size,
                        null
                )
        )
        .assertNext(result -> {
            assertTrue(result.content().isEmpty());
            assertFalse(result.hasNext());
            assertNull(result.nextCursor());
        })
        .verifyComplete();

        // Then
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(reactiveMongoTemplate).find(queryCaptor.capture(),eq(Message.class));

        Query capturedQuery = queryCaptor.getValue();
        Document queryObject = capturedQuery.getQueryObject();
        String queryStr = queryObject.toJson();

        assertTrue(queryStr.contains("$gte"),"La query deve contenere il filtro $gte");
        assertTrue(queryStr.contains("2026-01-01T10:00:00"),"La query deve contenere la data di inizio");

        // Keyset pagination: size + 1
        assertEquals(size + 1,capturedQuery.getLimit());
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
        
        verify(reactiveMongoTemplate).executeCommand(eq(RU_COMMAND));
    }

    @Test
    void searchMessages_NoFilters_EmptyQuery() {

        // Given
        int size = 20;

        when(reactiveMongoTemplate.find(any(Query.class),eq(Message.class)))
                .thenReturn(Flux.empty());

        when(reactiveMongoTemplate.executeCommand(anyString()))
                .thenReturn(Mono.just(new Document("RequestCharge", 1.5)));

        // When
        StepVerifier.create(
                messageRepository.searchMessages(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        size,
                        null
                )
        )
        .assertNext(result -> {
            assertTrue(result.content().isEmpty());
            assertFalse(result.hasNext());
            assertNull(result.nextCursor());
        })
        .verifyComplete();

        // Then
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        verify(reactiveMongoTemplate)
                .find(queryCaptor.capture(), eq(Message.class));

        Query capturedQuery = queryCaptor.getValue();

        assertTrue(capturedQuery.getQueryObject().isEmpty(),"La query non dovrebbe avere filtri");

        /*
        * Keyset pagination:
        * il repository chiede un elemento in più
        * per capire se esiste una pagina successiva.
        */
        assertEquals(size + 1, capturedQuery.getLimit());

        /*
        * Anche senza filtri la query deve avere il sort.
        */
        Document sortObject = capturedQuery.getSortObject();

        assertEquals(-1,sortObject.getInteger("messageRegistrationDate"));
        assertEquals( -1,sortObject.getInteger("_id"));
    }
}
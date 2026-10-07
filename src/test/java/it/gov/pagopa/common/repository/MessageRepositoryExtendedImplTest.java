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
import org.springframework.data.mongodb.core.aggregation.Aggregation;
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
        
        MessageSearchCursor cursor = new MessageSearchCursor(cursorDate, cursorId);
        Message message = new Message();
        message.setId(new ObjectId().toHexString());
        message.setMessageRegistrationDate(LocalDateTime.now().toString());

        // Mock della find
        when(reactiveMongoTemplate.find(any(Query.class), eq(Message.class)))
                .thenReturn(Flux.just(message));

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
        verify(reactiveMongoTemplate).find(queryCaptor.capture(), eq(Message.class));
        
        Query capturedQuery = queryCaptor.getValue();
        Document queryObject = capturedQuery.getQueryObject();

        // FILTRI PRINCIPALI
        assertTrue(queryObject.containsKey("$and"), "La query deve avere un operatore $and alla radice");
        List<Document> andList = (List<Document>) queryObject.get("$and");

        assertTrue(andList.stream().anyMatch(d -> messageId.equals(d.get(FIELD_MESSAGE_ID))));
        assertTrue(andList.stream().anyMatch(d -> recipientId.equals(d.get(FIELD_RECIPIENT_ID))));
        assertTrue(andList.stream().anyMatch(d -> originId.equals(d.get(FIELD_ORIGIN_ID))));

        // Verifica Date Range (dentro l'and)
        Document dateRangeCondition = andList.stream()
                .filter(d -> d.containsKey(FIELD_REGISTRATION_DATE) && d.get(FIELD_REGISTRATION_DATE) instanceof Document)
                .map(d -> (Document) d.get(FIELD_REGISTRATION_DATE))
                .filter(d -> d.containsKey("$gte")) // Distinguiamo dal cursore che usa $lt o $is
                .findFirst()
                .orElseThrow();
        assertEquals(startDate.format(formatter), dateRangeCondition.get("$gte"));
        assertEquals(endDate.format(formatter), dateRangeCondition.get("$lte"));

        // KEYSET CURSOR (Criteria aggiunto con query.addCriteria)
        Document keysetContainer = andList.stream()
                .filter(d -> d.containsKey("$or"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Manca l'operatore $or del keyset nella andList"));
        
        List<Document> cursorConditions = (List<Document>) keysetContainer.get("$or");
        assertEquals(2, cursorConditions.size());

        // Caso 1: data < cursorDate
        Document dateCursorCondition = cursorConditions.get(0);
        Document lessThanDate = (Document) dateCursorCondition.get(FIELD_REGISTRATION_DATE);
        assertEquals(cursorDate, lessThanDate.get("$lt"));

        // Caso 2: data == cursorDate AND _id < cursorId
        Document sameDateAndId = cursorConditions.get(1);
        assertTrue(sameDateAndId.containsKey("$and"));
        List<Document> sameDateConditions = (List<Document>) sameDateAndId.get("$and");
        
        Document sameDate = sameDateConditions.stream()
                .filter(c -> c.containsKey(FIELD_REGISTRATION_DATE))
                .findFirst().orElseThrow();
        assertEquals(cursorDate, sameDate.get(FIELD_REGISTRATION_DATE));

        Document idCondition = sameDateConditions.stream()
                .filter(c -> c.containsKey("_id"))
                .findFirst().orElseThrow();
        Document idLt = (Document) idCondition.get("_id");
        assertEquals(new ObjectId(cursorId), idLt.get("$lt"));

        // PAGINATION, SORT & PROJECTION
        assertEquals(size + 1, capturedQuery.getLimit());
    
        Document sortObject = capturedQuery.getSortObject();
        assertEquals(-1, sortObject.getInteger(FIELD_REGISTRATION_DATE));
        assertEquals(-1, sortObject.getInteger("_id"));

        Document fieldsObject = capturedQuery.getFieldsObject();
        assertEquals(1, fieldsObject.get("content"));
        assertEquals(1, fieldsObject.get("title"));
        assertEquals(1, fieldsObject.get("_id"));
        assertEquals(1, fieldsObject.get(FIELD_REGISTRATION_DATE));
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
        Document countResult = new Document("totaleMessaggi", 1L);
        
        when(reactiveMongoTemplate.aggregate(any(Aggregation.class), eq("message"), eq(Document.class)))
                .thenReturn(Flux.just(countResult));

        // When
        Mono<Long> result = messageRepository.countMessages(messageId, null, null, null, null);

        // Then
        StepVerifier.create(result)
                .expectNext(1L)
                .verifyComplete();

        ArgumentCaptor<Aggregation> aggCaptor = ArgumentCaptor.forClass(Aggregation.class);
        verify(reactiveMongoTemplate).aggregate(aggCaptor.capture(), eq("message"), eq(Document.class));
        
        Aggregation capturedAgg = aggCaptor.getValue();
        Document aggDoc = capturedAgg.toDocument("message", Aggregation.DEFAULT_CONTEXT);
        List<Document> pipeline = aggDoc.getList("pipeline", Document.class);

        assertTrue(pipeline.get(0).containsKey("$match"));
        assertTrue(pipeline.get(1).containsKey("$project"));
        assertTrue(pipeline.get(2).containsKey("$count"));

        Document hint = (Document) capturedAgg.getOptions().getHintObject().orElseThrow();
            assertEquals(1, hint.get("messageRegistrationDate"));        
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
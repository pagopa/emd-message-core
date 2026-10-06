package it.gov.pagopa.message.repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import it.gov.pagopa.message.dto.MessageCursorCodec;
import it.gov.pagopa.message.dto.MessageKeysetPage;
import it.gov.pagopa.message.dto.MessageSearchCursor;
import it.gov.pagopa.message.model.Message;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class MessageRepositoryExtendedImpl implements MessageRepositoryExtended {
    
    private static final String FIELD_MESSAGE_ID = "messageId";
    private static final String FIELD_RECIPIENT_ID = "recipientId";
    private static final String FIELD_ID = "_id";
    private static final String FIELD_ORIGIN_ID = "originId";
    private static final String FIELD_REGISTRATION_DATE = "messageRegistrationDate";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final MessageCursorCodec cursorCodec;

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public MessageRepositoryExtendedImpl(ReactiveMongoTemplate reactiveMongoTemplate,
            MessageCursorCodec cursorCodec) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.cursorCodec = cursorCodec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<MessageKeysetPage<Message>> searchMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate, MessageSearchCursor cursor, int size, Set<String> fields) {
        
        List<Criteria> allCriteria = buildCriteriaList(messageId, recipientId, originId, startDate, endDate);

        /*
         * Applichiamo il cursor solo dalla seconda richiesta in poi.
         */
        if (cursor != null) {
            ObjectId cursorObjectId = new ObjectId(cursor.id());
            Criteria keysetCriteria = new Criteria().orOperator(

                    
                    //Tutti i documenti con data precedente
                    Criteria.where(FIELD_REGISTRATION_DATE).lt(cursor.messageRegistrationDate()),

                    
                    //A parità di data, prendiamo gli _id precedenti
                    new Criteria().andOperator(
                            Criteria.where(FIELD_REGISTRATION_DATE).is(cursor.messageRegistrationDate()),
                            Criteria.where(FIELD_ID).lt(cursorObjectId)));

                allCriteria.add(keysetCriteria);
        }

        Query query = allCriteria.isEmpty() ? new Query() : new Query(new Criteria().andOperator(allCriteria.toArray(new Criteria[0])));

        /*
        * Keyset pagination:
        * ORDER BY messageRegistrationDate DESC, _id DESC
        */
        query.with(Sort.by(Sort.Order.desc(FIELD_REGISTRATION_DATE), Sort.Order.desc(FIELD_ID)));
        
        // Recupera size + 1 documenti per capire se esiste una pagina successiva.
        query.limit(size + 1);

        
        if (!CollectionUtils.isEmpty(fields)) {
            fields.forEach(field -> query.fields().include(field));
            query.fields().include(FIELD_ID)
                            .include(FIELD_REGISTRATION_DATE);
        }

        return reactiveMongoTemplate
        .find(query, Message.class)
        .collectList()
        .flatMap(messages -> getRequestCharge()
                            .defaultIfEmpty(0.0)
                            .doOnNext(ru -> log.info( "[MESSAGE-REPOSITORY][SEARCH] Search query completed - returned: {}, RU consumed: {}", Math.min(messages.size(), size), ru))
                            .map(ru -> {
                        boolean hasNext = messages.size() > size;
                        List<Message> content = hasNext ? messages.subList(0, size) : messages;
                        String nextCursor = null;
                        if (hasNext && !content.isEmpty()) {
                            Message lastMessage = content.get(content.size() - 1);
                            nextCursor = cursorCodec.encode(new MessageSearchCursor(lastMessage.getMessageRegistrationDate(), lastMessage.getId()));
                        }
                        return new MessageKeysetPage<>(content, hasNext, nextCursor);
                    }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Long> countMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate) {
        
        List<Criteria> criteriaList = buildCriteriaList(messageId, recipientId, originId, startDate, endDate);
        Criteria finalCriteria = criteriaList.isEmpty() ? new Criteria() : new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(finalCriteria),
                Aggregation.project(FIELD_REGISTRATION_DATE).andExclude(FIELD_ID),
                Aggregation.count().as("totaleMessaggi")
        ).withOptions(AggregationOptions.builder().hint(new Document(FIELD_REGISTRATION_DATE, 1)).build());

        return reactiveMongoTemplate.aggregate(aggregation, "message", Document.class)
            .next()
            .map(doc -> {
                Object total = doc.get("totaleMessaggi");
                return total instanceof Number ? ((Number) total).longValue() : 0L;
            })
            .defaultIfEmpty(0L)
            .flatMap(count -> getRequestCharge()
                    .defaultIfEmpty(0.0)
                    .map(ru -> {
                        log.info("[MESSAGE-REPOSITORY][COUNT] Count: {}, RU consumed: {}", count, ru);
                        return count;
                    })
            );
    }

    /**
     * Costruisce i criteri di ricerca in AND tra loro.
     */
    private List<Criteria> buildCriteriaList(String messageId, String recipientId, String originId,
                                    LocalDateTime startDate, LocalDateTime endDate) {
        List<Criteria> criteriaList = new ArrayList<>();

        // Filtro per Message ID
        if (StringUtils.hasText(messageId)) {
            criteriaList.add(Criteria.where(FIELD_MESSAGE_ID).is(messageId));
        }

        // Filtro per Codice Fiscale
        if (StringUtils.hasText(recipientId)) {
            criteriaList.add(Criteria.where(FIELD_RECIPIENT_ID).is(recipientId));
        }

        // Filtro per Origin ID
        if (StringUtils.hasText(originId)) {
            criteriaList.add(Criteria.where(FIELD_ORIGIN_ID).is(originId));
        }

        // Filtro per Intervallo Temporale
        if (startDate != null || endDate != null) {
            Criteria dateCriteria = Criteria.where(FIELD_REGISTRATION_DATE);
            if (startDate != null) {
                dateCriteria.gte(startDate.format(DATE_FORMATTER));
            }
            if (endDate != null) {
                dateCriteria.lte(endDate.format(DATE_FORMATTER));
            }
            criteriaList.add(dateCriteria);
        }

        return criteriaList;
    }

    /**
     * Test method, used to see the RU
     */
    private Mono<Double> getRequestCharge() {
    return reactiveMongoTemplate.executeCommand("{getLastRequestStatistics: 1}")
            .map(doc -> doc.getDouble("RequestCharge"));
}
}


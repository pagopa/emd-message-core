package it.gov.pagopa.message.repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import it.gov.pagopa.message.model.Message;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class MessageRepositoryExtendedImpl implements MessageRepositoryExtended {
    
    private static final String FIELD_MESSAGE_ID = "messageId";
    private static final String FIELD_RECIPIENT_ID = "recipientId";
    private static final String FIELD_ORIGIN_ID = "originId";
    private static final String FIELD_REGISTRATION_DATE = "messageRegistrationDate";

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public MessageRepositoryExtendedImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Message> searchMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate, int page, int size, Set<String> fields) {
        
        // Recent messages first
        Sort sort = Sort.by(Sort.Direction.DESC, FIELD_REGISTRATION_DATE);

        Query query = buildCriteriaQuery(messageId, recipientId, originId, startDate, endDate)
                .with(PageRequest.of(page, size, sort));

        if (!CollectionUtils.isEmpty(fields)) {
            fields.forEach(field -> query.fields().include(field));
            query.fields().include(FIELD_MESSAGE_ID);
        }
        return reactiveMongoTemplate.find(query, Message.class)
                .doOnTerminate(() ->
                        getRequestCharge().subscribe(ru -> log.info("Query searchMessages - RU consumate: " + ru)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Long> countMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate) {
        return reactiveMongoTemplate.count(buildCriteriaQuery(messageId, recipientId, originId, startDate, endDate), Message.class)
                                    .flatMap(count ->
                                            getRequestCharge().map(ru -> {
                                                log.info("Count operation - RU consumate: " + ru);
                                                return count;
                                            })
                                        );
    }

    /**
     * Costruisce i criteri di ricerca in AND tra loro.
     */
    private Query buildCriteriaQuery(String messageId, String recipientId, String originId,
                                    LocalDateTime startDate, LocalDateTime endDate) {
        Query query = new Query();
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
                dateCriteria.gte(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            }
            if (endDate != null) {
                dateCriteria.lte(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            }
            criteriaList.add(dateCriteria);
        }

        // Applichiamo tutti i criteri in AND
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return query;
    }

    /**
     * Test method, used to see the RU
     */
    private Mono<Double> getRequestCharge() {
    return reactiveMongoTemplate.executeCommand("{getLastRequestStatistics: 1}")
            .map(doc -> doc.getDouble("RequestCharge"));
}
}


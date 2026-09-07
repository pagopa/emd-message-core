package it.gov.pagopa.message.repository;

import java.time.LocalDateTime;
import java.util.Set;

import it.gov.pagopa.message.model.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Custom repository fragment for advanced/paginated message queries that cannot be expressed
 * with Spring Data derived query methods, and that require pagination to be pushed down
 * to the database (skip/limit) instead of loading the whole dataset in memory.
 */
public interface MessageRepositoryExtended {
    
    Flux<Message> searchMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate, int page, int size, Set<String> fields);

    Mono<Long> countMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate);
}

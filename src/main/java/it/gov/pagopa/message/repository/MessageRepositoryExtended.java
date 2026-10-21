package it.gov.pagopa.message.repository;

import java.time.LocalDateTime;
import java.util.Set;

import it.gov.pagopa.message.dto.MessageKeysetPage;
import it.gov.pagopa.message.dto.MessageSearchCursor;
import it.gov.pagopa.message.model.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Custom repository fragment for advanced/paginated message queries that cannot be expressed
 * with Spring Data derived query methods, and that require pagination to be pushed down
 * to the database (skip/limit) instead of loading the whole dataset in memory.
 */
public interface MessageRepositoryExtended {
    
    /**
     * Retrieves a reactive stream of {@link Message} entities filtered by the provided criteria.
     * Results are sorted by registration date in descending order and support pagination and field projection.
     *
     * @param messageId     optional filter for the exact message identifier
     * @param recipientId   optional filter for the exact recipient identifier
     * @param originId      optional filter for the exact origin system identifier
     * @param startDate     optional lower bound (inclusive) for the message registration date
     * @param endDate       optional upper bound (inclusive) for the message registration date
     * @param cursor        cursor identifying the last element of the previous page
     * @param size          the number of elements per page
     * @param fields        the set of specific document fields to include in the result (projection)
     * @return a {@link Flux} emitting the messages matching the criteria
     */
    public Mono<MessageKeysetPage<Message>> searchMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate,  MessageSearchCursor cursor, int size, Set<String> fields);

    /**
     * Calculates the total number of messages matching the provided filtering criteria.
     * This is used primarily to compute total pages for pagination metadata.
     *
     * @param messageId     optional filter for the exact message identifier
     * @param recipientId   optional filter for the exact recipient identifier
     * @param originId      optional filter for the exact origin system identifier
     * @param startDate     optional lower bound (inclusive) for the date range
     * @param endDate       optional upper bound (inclusive) for the date range
     * @return a {@link Mono} emitting the total count of matching messages
     */
    Mono<Long> countMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate);
}

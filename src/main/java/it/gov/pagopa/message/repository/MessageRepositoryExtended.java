package it.gov.pagopa.message.repository;

import java.util.Set;

import it.gov.pagopa.message.dto.MessageKeysetPage;
import it.gov.pagopa.message.dto.MessageSearchCursor;
import it.gov.pagopa.message.dto.MessageSearchParameter;
import it.gov.pagopa.message.model.Message;
import reactor.core.publisher.Mono;

/**
 * Custom repository fragment for advanced/paginated message queries that cannot be expressed
 * with Spring Data derived query methods, and that require pagination to be pushed down
 * to the database (skip/limit) instead of loading the whole dataset in memory.
 */
public interface MessageRepositoryExtended {
    
    /**
     * Retrieves a reactive stream of {@link Message} entities filtered by the provided criteria.
     * Results are sorted by registration date in descending order and support keyset pagination and field projection.
     *
     * @param params      the object containing filter criteria (messageId, recipientId, originId, state, dates)
     * @param cursor      cursor identifying the last element of the previous page
     * @param size        the number of elements per page
     * @param fields      the set of specific document fields to include in the result (projection)
     * @return a {@link Mono} emitting the messages matching the criteria wrapped in a {@link MessageKeysetPage}
     */
    public Mono<MessageKeysetPage<Message>> searchMessages(MessageSearchParameter params, MessageSearchCursor cursor, int size, Set<String> fields);


    /**
     * Calculates the total number of messages matching the provided filtering criteria.
     * This is used primarily to compute total pages for pagination metadata.
     *
     * @param params the object containing filter criteria
     * @return a {@link Mono} emitting the total count of matching messages
     */
    Mono<Long> countMessages(MessageSearchParameter params);

}

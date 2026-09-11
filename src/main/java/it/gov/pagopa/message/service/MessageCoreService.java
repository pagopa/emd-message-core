package it.gov.pagopa.message.service;


import java.time.LocalDateTime;
import java.util.List;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.MessageSearchResponseDTO;
import reactor.core.publisher.Mono;

/**
 * <p>Service contract for message delivery orchestration.</p>
 */
public interface MessageCoreService {

    /**
     * <p>Validates recipient and enqueues message for notification delivery.</p>
     *
     * @param messageDTO the message to be queued for delivery
     * @return {@code Mono<Boolean>}
     * <ul>
     *   <li>{@code true} if successfully queued,</li>
     *   <li>{@code false} if no channels enabled</li>
     * </ul>
     */
    Mono<Boolean> send(MessageDTO messageDTO);

    /**
     * Searches Messages filtering by exact {@code messageId}, {@code recipientId}, {@code originId} or messages sent
     * between {@code startDate} and {@code endDate}, returning a paginated result.
     * <p>
     * The returned content only contains the fields requested via {@code fields} (matching
     * {@link it.gov.pagopa.message.constants.MessageCoreConstants.SearchFields}), reducing the response
     * payload size.
     *
     * @param messageId     the exact message identifier to match (nullable)
     * @param recipientId   the exact recipient identifier to match (nullable)
     * @param originId      the exact origin identifier to match (nullable)
     * @param startDate     the start date for the message sent range (nullable)
     * @param endDate       the end date for the message sent range (nullable)
     * @param page         the zero-based page index
     * @param size         the requested page size
     * @param fields       optional override of the fields to return for each TPP (nullable/empty
     *                     falls back to the default grid fields)
     * @return a {@link Mono} containing the paginated {@link MessageSearchResponseDTO}
     */
    Mono<MessageSearchResponseDTO> searchMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate, 
        int page, int size, List<String> fields);
}
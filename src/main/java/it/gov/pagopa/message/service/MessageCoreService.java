package it.gov.pagopa.message.service;


import java.time.LocalDateTime;
import java.util.List;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.MessageSearchResponseDTO;
import it.gov.pagopa.message.dto.ResponseMessageDTO;
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
     * @param fields       optional override of the fields to return for each Message (nullable/empty
     *                     falls back to the default grid fields)
     * @return a {@link Mono} containing the paginated {@link MessageSearchResponseDTO}
     */
    Mono<MessageSearchResponseDTO> searchMessages(String messageId, String recipientId, String originId, LocalDateTime startDate, LocalDateTime endDate, 
        int page, int size, List<String> fields);

    /**
     * <p>Retrieves a message from the storage by its unique identifier.</p>
     *
     * @param entityId the identifier of the tpp
     * @param messageId the identifier of the message to retrieve
     * @return a {@code Mono} emitting the {@link ResponseMessageDTO} if found
     *         (emits an error signal if the message does not exist)
     */
    Mono<ResponseMessageDTO> getMessage(String entityId, String messageId);
        
    /**
     * <p>Deletes a Message by entityId and messageId.</p>
     * 
     * @param entityId the identifier of the tpp
     * @param messageId the identifier of the message
     * @return a {@link Mono<Void>} that completes when the deletion is successful
     */
    Mono<Void> deleteMessage(String entityId, String messageId);
}
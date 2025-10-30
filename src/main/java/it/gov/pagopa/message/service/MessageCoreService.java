package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
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
}
package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import reactor.core.publisher.Mono;

/**
 * <p>Service for enqueuing messages to the message broker.</p>
 *
 * <p>Prepares messages for asynchronous delivery by separate consumer services.</p>
 */
public interface MessageProducerService {

     /**
      * <p>Enqueues a message to the message broker for TPP notification delivery.</p>
      *
      * <p>The actual delivery to TPP is handled by a separate consumer service.</p>
      *
      * @param messageDTO the message to be enqueued
      * @param messageId the unique identifier of the message
      * @return {@code Mono<Void>} completes when the message has been enqueued
      */
     Mono<Void> enqueueMessage(MessageDTO messageDTO, String messageId);
}

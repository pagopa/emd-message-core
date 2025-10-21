package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import reactor.core.publisher.Mono;

public interface MessageProducerService {

     /**
      * Enqueues the message to the message broker to start the actual delivery to TPP by a separate consumer service.
      *
      * @param messageDTO the message to be enqueued
      * @param messageId the unique identifier of the message
      * @return a Mono that completes when the message has been enqueued
      */
     Mono<Void> enqueueMessage(MessageDTO messageDTO, String messageId);
}
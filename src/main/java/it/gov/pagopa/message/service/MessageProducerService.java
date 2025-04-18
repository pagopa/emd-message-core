package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import reactor.core.publisher.Mono;

public interface MessageProducerService {
     Mono<Void> enqueueMessage(MessageDTO messageDTO, String messageId);
}

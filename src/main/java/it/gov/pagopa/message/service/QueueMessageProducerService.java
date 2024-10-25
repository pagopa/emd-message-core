package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import reactor.core.publisher.Mono;

public interface QueueMessageProducerService {

     Mono<Void> enqueueMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, String entityId);
     void enqueueMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, String entityId, long retry);
}

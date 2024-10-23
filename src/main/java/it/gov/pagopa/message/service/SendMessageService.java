package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import reactor.core.publisher.Mono;

public interface SendMessageService {
    Mono<Void> sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, String entityId);
    Mono<Void> sendMessage(MessageDTO messageDTO, String messageUrl, String authenticationUrl, String entityId, long retry);

}

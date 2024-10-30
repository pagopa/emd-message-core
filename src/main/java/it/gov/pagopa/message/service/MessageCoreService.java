package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import reactor.core.publisher.Mono;

public interface MessageCoreService {

    Mono<Boolean> sendMessage(MessageDTO messageDTO);
}

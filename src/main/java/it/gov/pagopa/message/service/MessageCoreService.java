package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.model.Outcome;
import reactor.core.publisher.Mono;

public interface MessageCoreService {

    Mono<Outcome> sendMessage(MessageDTO messageDTO);
}

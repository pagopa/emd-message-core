package it.gov.pagopa.message.service;


import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

public interface MessageConsumerService {
    void execute(Flux<Message<String>> messageFlux);

}

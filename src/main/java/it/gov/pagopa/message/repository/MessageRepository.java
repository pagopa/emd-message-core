package it.gov.pagopa.message.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import it.gov.pagopa.message.model.Message;
import reactor.core.publisher.Mono;

public interface MessageRepository extends ReactiveMongoRepository<Message,String>{
    
    Mono<Message> findById(String messageId);
    
}
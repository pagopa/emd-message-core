package it.gov.pagopa.message.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import it.gov.pagopa.message.model.Message;
import reactor.core.publisher.Mono;

public interface MessageRepository extends ReactiveMongoRepository<Message,String>{
    
    /**
     * <p>Retrieves a message using its business identifier ({@code messageId}).</p>
     *
     * @param messageId the business identifier of the message
     * @return a {@code Mono} emitting the {@link Message} if found, or empty otherwise
     */
    Mono<Message> findByMessageId(String messageId);
    
}
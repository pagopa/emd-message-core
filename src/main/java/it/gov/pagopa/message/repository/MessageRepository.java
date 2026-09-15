package it.gov.pagopa.message.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import it.gov.pagopa.message.model.Message;
import reactor.core.publisher.Mono;


public interface MessageRepository extends ReactiveMongoRepository<Message,String>, MessageRepositoryExtended{
    
    /**
     * <p>Retrieves a message using its business identifier ({@code messageId}).</p>
     *
     * @param messageId the business identifier of the message
     * @return a {@code Mono} emitting the {@link Message} if found, or empty otherwise
     */
    Mono<Message> findByMessageId(String messageId);

    /**
     * <p>Retrieves a message using ({@code entityId}) and ({@code messageId}).</p>
     * 
     * @param entityId entityId the identifier of the tpp
     * @param messageId messageId the identifier of the message
     * @return a {@code Mono} emitting the {@link Message} if found, or empty otherwise
     */
    Mono<Message> findByEntityIdAndMessageId(String entityId, String messageId);
    
}
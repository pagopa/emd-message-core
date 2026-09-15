package it.gov.pagopa.message.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import it.gov.pagopa.message.model.Message;
import reactor.core.publisher.Mono;

public interface MessageRepository extends ReactiveMongoRepository<Message,String>, MessageRepositoryExtended {
    
    /**
     * <p>Deletes a message using ({@code entityId}) and ({@code messageId}).</p>
     * 
     * @param entityId the identifier of the entity
     * @param messageId the identifier of the message
     * @return an empty {@code Mono} upon successful deletion
     */
    Mono<Long>deleteByEntityIdAndMessageId(String entityId, String messageId);
    
}
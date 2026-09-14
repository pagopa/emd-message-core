package it.gov.pagopa.message.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import it.gov.pagopa.message.model.Message;

public interface MessageRepository extends ReactiveMongoRepository<Message,String>, MessageRepositoryExtended {
    

    
}
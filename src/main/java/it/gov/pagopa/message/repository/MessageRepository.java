package it.gov.pagopa.message.repository;



import it.gov.pagopa.message.model.Message;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface MessageRepository extends ReactiveMongoRepository<Message,String> {

    Flux<Message> findByHashedFiscalCode(String hashedFiscalCode);


}

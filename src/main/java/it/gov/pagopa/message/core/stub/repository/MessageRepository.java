package it.gov.pagopa.message.core.stub.repository;



import it.gov.pagopa.message.core.stub.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;

public interface MessageRepository extends MongoRepository<Message,String> {
    ArrayList<Message> findByHashedFiscalCode(String hashedFiscalCode);


}

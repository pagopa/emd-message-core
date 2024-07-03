package it.gov.pagopa.message.core.repository;


import it.gov.pagopa.message.core.model.CitizenConsent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;

public interface CitizenRepository extends MongoRepository<CitizenConsent,String> {
    ArrayList<CitizenConsent> findByHashedFiscalCodeAndChannelStateTrue(String hashedFiscalCode);

}

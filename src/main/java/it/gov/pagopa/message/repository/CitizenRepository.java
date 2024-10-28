package it.gov.pagopa.message.repository;


import it.gov.pagopa.message.model.CitizenConsent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CitizenRepository extends ReactiveMongoRepository<CitizenConsent, String> {

}

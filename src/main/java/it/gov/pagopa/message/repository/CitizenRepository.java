package it.gov.pagopa.message.repository;


import it.gov.pagopa.message.model.CitizenConsent;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CitizenRepository extends ReactiveMongoRepository<CitizenConsent, String> {
    @Query(value = "{}", fields = "{ 'fiscalCode' : 1 }")
    Flux<String> findAllFiscalCodes();

}

package it.gov.pagopa.message.connector.citizen;

import it.gov.pagopa.message.dto.CitizenConsentDTO;
import reactor.core.publisher.Mono;

import java.util.List;
public interface CitizenConnector {
    Mono<List<CitizenConsentDTO>> getCitizenConsentsEnabled(String fiscalCode);


}

package it.gov.pagopa.message.connector;

import reactor.core.publisher.Mono;
public interface CitizenConnector {
    Mono<String> checkFiscalCode(String fiscalCode);


}

package it.gov.pagopa.message.connector;

import reactor.core.publisher.Mono;

/**
 *  Connector for interacting with the emd-citizen service.
 */
public interface CitizenConnector {

  /**
   * Checks if a fiscal code exists in the BloomFilter.
   *
   * @param fiscalCode the fiscal code to verify
   * @return a Mono containing the verification response
   */
  Mono<String> checkFiscalCode(String fiscalCode);


}

package it.gov.pagopa.message.connector;

import reactor.core.publisher.Mono;

/**
 * <p>Connector for interacting with the emd-citizen service.</p>
 *
 * <p>Provides remote operations to verify citizen consent status.</p>
 */
public interface CitizenConnector {

  /**
   * <p>Checks if a fiscal code exists in the Bloom filter and has enabled consents.</p>
   * <p>Delegates to the emd-citizen service filter endpoint.</p>
   *
   * @param fiscalCode the fiscal code to verify
   * @return {@code Mono<String>} verification response status ("OK" or "NO CHANNELS ENABLED")
   */
  Mono<String> checkFiscalCode(String fiscalCode);


}

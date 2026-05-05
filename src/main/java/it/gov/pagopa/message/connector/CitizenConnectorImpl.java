package it.gov.pagopa.message.connector;

import it.gov.pagopa.common.configuration.WebClientRetrySpecs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

/**
 * <p>Implementation of {@link CitizenConnector}.</p>
 */
@Service
@Slf4j
public class CitizenConnectorImpl implements CitizenConnector {

    private final WebClient webClient;

    /**
     * @param webClientBuilder pre-configured builder from {@code WebClientConfig}
     * @param baseUrl          base URL of the emd-citizen service
     */
    public CitizenConnectorImpl(
            WebClient.Builder webClientBuilder,
            @Value("${rest-client.citizen.baseUrl}") String baseUrl) {

        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Idempotent GET — uses the permissive
     * {@link WebClientRetrySpecs#transientNetwork()} retry policy: any
     * {@link WebClientRequestException} (including stale-connection drops) is
     * retried with exponential back-off + jitter.
     */
    @Override
    public Mono<String> checkFiscalCode(String fiscalCode) {
        return webClient.get()
                .uri("/emd/citizen/filter/{fiscalCode}", fiscalCode)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(WebClientRetrySpecs.transientNetwork())
                .doOnError(ex -> log.error(
                        "[CITIZEN-CONNECTOR] GET /emd/citizen/filter/{{fiscalCode}} failed: {}",
                        ex.getMessage()));
    }
}

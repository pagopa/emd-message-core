package it.gov.pagopa.message.connector;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * <p>Implementation of {@link CitizenConnector}.</p>
 *
 * <p>Uses {@link WebClient} to perform HTTP calls to the emd-citizen service.</p>
 */
@Service
public class CitizenConnectorImpl implements CitizenConnector {

    private final WebClient webClient;
    public CitizenConnectorImpl( @Value("${rest-client.citizen.baseUrl}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();

    }

    /**
     *  {@inheritDoc}
     *
     * @param fiscalCode the fiscal code to verify
     * @return {@code Mono<String>} status from emd-citizen service
     */
    @Override
    public Mono<String> checkFiscalCode(String fiscalCode) {
        return webClient.get()
                .uri("/emd/citizen/filter/{fiscalCode}",fiscalCode)
                .retrieve()
                .bodyToMono(String.class);
    }
}

package it.gov.pagopa.message.connector.citizen;


import it.gov.pagopa.message.dto.CitizenConsentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CitizenConnectorImpl implements CitizenConnector {

    @Value("${rest-client.citizen.baseUrl}")
    private String baseUrl;

    private final WebClient webClient;
    public CitizenConnectorImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<List<CitizenConsentDTO>> getCitizenConsentsEnabled(String fiscalCode) {
        return webClient.get()
                .uri("/emd/citizen/list/{fiscalCode}/enabled", fiscalCode)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }
}

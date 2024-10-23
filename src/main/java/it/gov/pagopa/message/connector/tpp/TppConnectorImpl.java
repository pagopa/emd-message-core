package it.gov.pagopa.message.connector.tpp;


import it.gov.pagopa.message.dto.TppDTO;
import it.gov.pagopa.message.dto.TppIdList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TppConnectorImpl implements  TppConnector {
    private final WebClient webClient;


    @Value("${rest-client.tpp.baseUrl}")
    private String baseUrl;


    public TppConnectorImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<List<TppDTO>> getTppsEnabled(TppIdList tppIdList) {
        return webClient.post()
                .uri("/emd/tpp/list")
                .bodyValue(tppIdList)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });

    }
}

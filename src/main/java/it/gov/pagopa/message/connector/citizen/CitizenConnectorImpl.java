package it.gov.pagopa.message.connector.citizen;


import feign.FeignException;
import it.gov.pagopa.message.custom.CitizenInvocationException;
import it.gov.pagopa.message.dto.CitizenConsentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CitizenConnectorImpl implements CitizenConnector {

    private final CitizenFeignClient citizenFeignClient;

    public CitizenConnectorImpl(CitizenFeignClient citizenFeignClient) {
        this.citizenFeignClient = citizenFeignClient;
    }

    @Override
    public Mono<List<CitizenConsentDTO>> getCitizenConsentsEnabled(String fiscalCode) {
        return Mono.fromCallable(() -> citizenFeignClient.getCitizenConsentsEnabled(fiscalCode))
                .onErrorMap(FeignException.class, feignException -> new CitizenInvocationException())
                .map(ResponseEntity::getBody);
    }
}

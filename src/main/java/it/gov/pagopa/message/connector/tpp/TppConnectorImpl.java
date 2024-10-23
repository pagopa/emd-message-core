package it.gov.pagopa.message.connector.tpp;



import feign.FeignException;
import it.gov.pagopa.message.custom.TppInvocationException;
import it.gov.pagopa.message.dto.TppDTO;
import it.gov.pagopa.message.dto.TppIdList;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TppConnectorImpl implements  TppConnector {
    private final TppFeignClient tppFeignClient;

    public TppConnectorImpl(TppFeignClient tppFeignClient) {
        this.tppFeignClient = tppFeignClient;
    }

    public Mono<List<TppDTO>> getTppsEnabled(TppIdList tppIdList) {
        return Mono.fromCallable(() -> tppFeignClient.getTppsEnabled(tppIdList))
                .onErrorMap(FeignException.class, feignException -> new TppInvocationException())
                .map(ResponseEntity::getBody);
    }
}

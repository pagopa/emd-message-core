package it.gov.pagopa.message.connector.tpp;

import it.gov.pagopa.message.dto.TppDTO;
import it.gov.pagopa.message.dto.TppIdList;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TppConnector {
    Mono<List<TppDTO>> getTppsEnabled(TppIdList tppIdListIds);
}
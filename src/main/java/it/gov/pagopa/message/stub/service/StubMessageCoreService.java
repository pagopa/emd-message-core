package it.gov.pagopa.message.stub.service;

import it.gov.pagopa.message.dto.MessageDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
public interface StubMessageCoreService {

    Mono<List<MessageDTO>> getMessages(String fiscalCode);


}

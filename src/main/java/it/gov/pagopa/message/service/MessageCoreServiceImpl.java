package it.gov.pagopa.message.service;

import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import it.gov.pagopa.message.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;



@Slf4j
@Service
public class MessageCoreServiceImpl implements MessageCoreService {

    private final CitizenConnectorImpl citizenConnector;

    private final MessageProducerServiceImpl messageProducerService;

    public MessageCoreServiceImpl(CitizenConnectorImpl citizenConnector,
                                  MessageProducerServiceImpl messageProducerService) {
        this.citizenConnector = citizenConnector;
        this.messageProducerService = messageProducerService;
    }


    @Override
    public Mono<Boolean> send(MessageDTO messageDTO) {
        log.info("[EMD-MESSAGE-CORE][SEND] Received message: {}", messageDTO);
        return citizenConnector.checkFiscalCode(messageDTO.getRecipientId())
                .flatMap(response -> "OK".equals(response) ?
                    messageProducerService.enqueueMessage(messageDTO).thenReturn(true) :
                    Mono.just(false));
    }
}



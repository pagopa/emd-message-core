package it.gov.pagopa.message.service;

import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import it.gov.pagopa.message.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static it.gov.pagopa.common.utils.CommonUtilities.createSHA256;
import static it.gov.pagopa.common.utils.CommonUtilities.inputSanitization;


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
        String messageId = inputSanitization(messageDTO.getMessageId());
        String recipientIdHashed = createSHA256(inputSanitization(messageDTO.getRecipientId()));
        log.info("[MESSAGE-CORE][SEND] Received message: {}", messageId);

        return citizenConnector.checkFiscalCode(messageDTO.getRecipientId())
                .flatMap(response -> {
                    if ("OK".equals(response)) {
                        log.info("[MESSAGE-CORE][SEND] Fiscal code check passed for recipient: {}", recipientIdHashed);
                        return messageProducerService.enqueueMessage(messageDTO,messageId)
                                .doOnSuccess(aVoid -> log.info("[MESSAGE-CORE][SEND] Message {} enqueued successfully for recipient: {}",messageId,recipientIdHashed))
                                .thenReturn(true);
                    } else {
                        log.warn("[MESSAGE-CORE][SEND] Fiscal code check failed for recipient: {}", recipientIdHashed);
                        return Mono.just(false);
                    }
                })
                .doOnError(error -> log.error("[MESSAGE-CORE][SEND] Error while checking fiscal code for recipient: {}. Error: {}", recipientIdHashed, error.getMessage()));
    }

}



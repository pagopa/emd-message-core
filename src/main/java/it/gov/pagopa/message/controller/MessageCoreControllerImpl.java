package it.gov.pagopa.message.controller;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.MessageSearchResponseDTO;
import it.gov.pagopa.message.dto.SendResponseDTO;
import it.gov.pagopa.message.service.MessageCoreServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * <p>Implementation of the reactive REST contract defined by {@link MessageCoreController}</p>
 */
@RestController
public class MessageCoreControllerImpl implements MessageCoreController {
    private final MessageCoreServiceImpl messageCoreService;

    public MessageCoreControllerImpl(MessageCoreServiceImpl messageCoreService) {
        this.messageCoreService = messageCoreService;
    }

    /**
     * {@inheritDoc}
     */
    public Mono<ResponseEntity<SendResponseDTO>> send(MessageDTO messageDTO) {
        return messageCoreService.send(messageDTO)
                .map(outcome -> Boolean.TRUE.equals(outcome) ?
                        ResponseEntity.ok(new SendResponseDTO("OK")) :
                        ResponseEntity.status(HttpStatus.ACCEPTED).body(new SendResponseDTO("NO_CHANNELS_ENABLED")));
    }

    /**
     * {@inheritDoc}
     */
    public Mono<ResponseEntity<MessageSearchResponseDTO>> searchMessages(String messageId, String recipientId, String originId,
            LocalDateTime startDate, LocalDateTime endDate, int page, int size, List<String> fields) {
        return messageCoreService.searchMessages(messageId, recipientId, originId, startDate, endDate, page, size, fields)
                .map(ResponseEntity::ok);
    }

}

package it.gov.pagopa.message.controller;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.service.MessageCoreServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class MessageCoreControllerImpl implements MessageCoreController {
    private final MessageCoreServiceImpl messageCoreService;

    public MessageCoreControllerImpl(MessageCoreServiceImpl messageCoreService) {
        this.messageCoreService = messageCoreService;
    }

    public Mono<ResponseEntity<String>> send(MessageDTO messageDTO) {
        return messageCoreService.send(messageDTO)
                .map(outcome -> Boolean.TRUE.equals(outcome) ?
                        ResponseEntity.ok("OK") :
                        ResponseEntity.status(HttpStatus.ACCEPTED).body("NO CHANNELS ENABLED"));
    }
}

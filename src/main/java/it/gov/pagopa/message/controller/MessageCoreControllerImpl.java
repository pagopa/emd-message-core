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

    public Mono<ResponseEntity<String>> sendMessage(MessageDTO messageDTO) {
        return messageCoreService.sendMessage(messageDTO)
                .map(outcome -> {
                    if (Boolean.TRUE.equals(outcome))
                        return ResponseEntity.ok("OK");
                    else
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body("KO");
                });
    }
}

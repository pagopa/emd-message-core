package it.gov.pagopa.message.core.controller;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.dto.Outcome;
import it.gov.pagopa.message.core.enums.OutcomeStatus;
import it.gov.pagopa.message.core.service.MessageCoreServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageCoreControllerImpl implements MessageCoreController {
    private final MessageCoreServiceImpl messageCoreService;

    public MessageCoreControllerImpl(MessageCoreServiceImpl messageCoreService) {
        this.messageCoreService = messageCoreService;
    }

    @Override
    public ResponseEntity<Outcome> sendMessage(MessageDTO messageDTO) {
        Outcome outcome = messageCoreService.sendMessage(messageDTO);
        if(outcome.getOutcomeStatus().equals(OutcomeStatus.OK))
            return ResponseEntity.ok(outcome);
        else
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(outcome);
    }
}

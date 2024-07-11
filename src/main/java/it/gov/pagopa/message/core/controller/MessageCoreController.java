package it.gov.pagopa.message.core.controller;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.dto.Outcome;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/emd/message-core")
public interface MessageCoreController {

    /**
     * Send message
     *
     * @param messageDTO message to be sent to the tpp
     * @return outcome of sending the message
     */
    @PostMapping("/sendMessage")
    ResponseEntity<Outcome> sendMessage(@Valid @RequestBody MessageDTO messageDTO);

}

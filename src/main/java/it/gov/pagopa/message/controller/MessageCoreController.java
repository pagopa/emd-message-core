package it.gov.pagopa.message.controller;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.model.Outcome;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;


@RequestMapping("/emd/message")
public interface MessageCoreController {

    /**
     * Send message
     *
     * @param messageDTO to be sent to the tpp
     * @return outcome of sending the message
     */
    @PostMapping("/sendMessage")
    Mono<ResponseEntity<Outcome>> sendMessage(@Valid @RequestBody MessageDTO messageDTO);

}

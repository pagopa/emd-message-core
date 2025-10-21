package it.gov.pagopa.message.controller;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.SendResponseDTO;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;


@RequestMapping("/emd/message-core")
public interface MessageCoreController {

    /**
     * Starts the process to deliver the notification of the message to the TPP.
     *
     * @param messageDTO the message to be queued for delivery
     * @return HTTP 200 with outcome "OK" if successfully queued, <br>
     *         HTTP 202 with "NO_CHANNELS_ENABLED" if no notification channels are available for the recipient
     */
    @PostMapping("/sendMessage")
    Mono<ResponseEntity<SendResponseDTO>> send(@Valid @RequestBody MessageDTO messageDTO);
}

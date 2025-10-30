package it.gov.pagopa.message.controller;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.SendResponseDTO;
import it.gov.pagopa.message.service.MessageCoreService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * <p>Reactive REST contract exposing message delivery operations.</p>
 *
 * <p>Error semantics and domain flows are documented in the service layer; controller focuses on HTTP contract.</p>
 */
@RequestMapping("/emd/message-core")
public interface MessageCoreController {

    /**
     * <p>Starts the process to deliver the notification of the message to the TPP.</p>
     * <p>Delegates to {@link MessageCoreService#send(MessageDTO)}.</p>
     * <p>Endpoint: {@code POST /emd/message-core/sendMessage}</p>
     *
     * @param messageDTO the message to be queued for delivery (validated)
     * @return {@code Mono<ResponseEntity<SendResponseDTO>>}
     *        <ul>
     *          <li> 200 OK with outcome {@code "OK"} if successfully queued, </li>
     *          <li> 202 Accepted with {@code "NO_CHANNELS_ENABLED"} if recipient has no active channels</li>
     *        </ul>
     */
    @PostMapping("/sendMessage")
    Mono<ResponseEntity<SendResponseDTO>> send(@Valid @RequestBody MessageDTO messageDTO);
}

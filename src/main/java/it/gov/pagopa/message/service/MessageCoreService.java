package it.gov.pagopa.message.service;


import it.gov.pagopa.message.dto.MessageDTO;
import reactor.core.publisher.Mono;

public interface MessageCoreService {

    /**
     * Check before adding message on kafka queue for notification delivery
     *
     * @param messageDTO the message to be queued for delivery
     * @return HTTP 200 with outcome "OK" if successfully queued, <br>
     *         HTTP 202 with "NO_CHANNELS_ENABLED" if no notification channels are available for the recipient
     */
    Mono<Boolean> send(MessageDTO messageDTO);
}
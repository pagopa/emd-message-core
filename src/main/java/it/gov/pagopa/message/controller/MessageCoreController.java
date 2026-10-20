package it.gov.pagopa.message.controller;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.MessageSearchParameter;
import it.gov.pagopa.message.dto.MessageSearchResponseDTO;
import it.gov.pagopa.message.dto.ResponseMessageDTO;
import it.gov.pagopa.message.dto.SendResponseDTO;
import it.gov.pagopa.message.service.MessageCoreService;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * Searches for Messages applying the filters provided in the request and returning a paginated result.
     * <p>
     * The content of each result only contains the fields requested via {@code fields} (matching
     * the properties of {@link MessageSearchResponseDTO}), reducing the response payload size.
     * When {@code fields} is omitted, the default grid fields are returned.
     *
     * @param request the object containing all search filters (messageId, recipientId, originId, state, dates),
     *                pagination (cursor, size) and projection fields
     * @return a {@link Mono} containing a {@link ResponseEntity} with the paginated
     *          {@link MessageSearchResponseDTO}
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<MessageSearchResponseDTO>> searchMessages(
        @ModelAttribute MessageSearchParameter request
    );

    /**
     * <p>Retrieves the details of a specific message using its unique identifier.</p>
     * <p>Delegates to {@link MessageCoreService#getMessage(String)}.</p>
     * <p>Endpoint: {@code GET /emd/message-core/{entityId}/{messageId}}</p>
     *
     * @param entityId the identifier of the tpp
     * @param messageId the identifier of the message to retrieve
     * @return {@code Mono<ResponseEntity<ResponseMessageDTO>>}
     *        <ul>
     *          <li> 200 OK with the {@link ResponseMessageDTO} body if the message is found, </li>
     *          <li> 404 Not Found with {@code "MESSAGE_NOT_FOUND"} if no message matches the provided IDs</li>
     *        </ul>
     */
    @GetMapping("/{entityId}/{messageId}")
    Mono<ResponseEntity<ResponseMessageDTO>> getMessage(@PathVariable String entityId, @PathVariable String messageId);

    /**
     * Delete a Message from Database by entityId and messageId.
     * 
     * @param entityId the identifier of the tpp
     * @param messageId the identifier of the message
     * @return an empty {@link Mono} resulting in a 204 No Content response
     */
    @DeleteMapping("/{entityId}/{messageId}")
    Mono<ResponseEntity<Void>> deleteMessage(@PathVariable String entityId, @PathVariable String messageId);
}

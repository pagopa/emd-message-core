package it.gov.pagopa.message.controller;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.MessageSearchResponseDTO;
import it.gov.pagopa.message.dto.SendResponseDTO;
import it.gov.pagopa.message.service.MessageCoreService;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
     * Search Messages by exact {@code messageId}, {@code recipientId}, {@code originId} or messages sent
     * between {@code startDate} and {@code endDate} and returning a paginated result.
     * <p>
     * At least one filter should be provided. When {@code entityId} is present it takes precedence
     * over {@code businessName}. The {@code size} is capped by a configured maximum to protect
     * database resources.
     * <p>
     * The content of each result only contains the fields requested via {@code fields} (matching
     * the properties of {@link MessageSearchResponseDTO}), reducing the response payload size.
     * When {@code fields} is omitted, the default grid fields are returned: {@code businessName},
     * {@code entityId}, {@code isPaymentEnabled}, {@code tppId}, {@code state}, {@code lastUpdateDate}.
     *
     * @param messageId     optional exact message identifier filter
     * @param recipientId   optional exact recipient identifier filter
     * @param originId      optional exact origin identifier filter
     * @param startDate     optional start date for message sent range
     * @param endDate       optional end date for message sent range
     * @param page         zero-based page index (default 0)
     * @param size         page size (default 10, capped by the configured maximum)
     * @param fields       optional override of the fields to return for each TPP
     * @return a {@link Mono} containing a {@link ResponseEntity} with the paginated
     *          {@link MessageSearchResponseDTO}
     */
    @GetMapping("/search")
    Mono<ResponseEntity<MessageSearchResponseDTO>> searchMessages(
            @RequestParam(required = false) String messageId,
            @RequestParam(required = false) String recipientId,
            @RequestParam(required = false) String originId,
            @RequestParam (required = false) LocalDateTime startDate,
            @RequestParam (required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<String> fields);
}

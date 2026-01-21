package it.gov.pagopa.message.controller;


import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.dto.SendResponseDTO;
import it.gov.pagopa.message.service.MessageCoreService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

/**
 * <p>Reactive REST contract exposing message delivery operations.</p>
 *
 * <p>Error semantics and domain flows are documented in the service layer; controller focuses on HTTP contract.</p>
 */
@Tag(
    name = "Notification Management", 
    description = "API per la gestione delle notifiche."
)
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
    @Operation(
        summary = "Starts the process to deliver the notification of the message to the TPP",
        description = "Starts the process to deliver the notification of the message to the TPP."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully queued",
            content = @Content(schema = @Schema(implementation = SendResponseDTO.class))),
        @ApiResponse(responseCode = "202", description = "No channels enabled",
            content = @Content(schema = @Schema(implementation = SendResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Error in the request syntax or semantics",
            content = @Content),
    })
    @PostMapping("/sendMessage")
    Mono<ResponseEntity<SendResponseDTO>> send(@Valid @RequestBody MessageDTO messageDTO);
}

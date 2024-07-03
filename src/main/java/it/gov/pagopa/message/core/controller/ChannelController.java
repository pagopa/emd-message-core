package it.gov.pagopa.message.core.controller;


import it.gov.pagopa.message.core.dto.ChannelDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/emd/onboarding-tpp")
public interface ChannelController {

    /**
     * Send message
     *
     * @param channelId to update
     * @return outcome of the removal
     */
    @DeleteMapping("/channel/{channelId}")
    ResponseEntity<ChannelDTO> deleteChannel(@Valid @PathVariable String channelId);

    /**
     * Send message
     *
     * @param channelId to update
     * @return outcome of the update
     */
    @PutMapping("/channel/{channelId}")
    ResponseEntity<ChannelDTO> updateChannel(@Valid @PathVariable String channelId);

    /**
     * Send message
     *
     * @param channelDTO to save
     * @return outcome of sending the save
     */
    @PostMapping("/channel")
    ResponseEntity<ChannelDTO> saveChannel(@Valid @RequestBody ChannelDTO channelDTO);

}

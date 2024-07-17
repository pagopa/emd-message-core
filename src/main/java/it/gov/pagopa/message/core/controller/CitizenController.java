package it.gov.pagopa.message.core.controller;


import it.gov.pagopa.message.core.dto.CitizenConsentDTO;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RequestMapping("/emd/onboarding-citizen")
public interface CitizenController {

    /**
     * Send message
     *MessageDispatcherUtils
     * @param userId to update
     * @param channelId to update
     * @return outcome of the removal
     */
    @DeleteMapping("/citizenConsents")
    ResponseEntity<CitizenConsentDTO> deleteCitizenConsents(@RequestParam String userId, @RequestParam String channelId);
    /**
     * Send message
     *
     * @param citizenConsentDTO to update
     * @return outcome of the update
     */
    @PutMapping("/citizenConsents")
    ResponseEntity<CitizenConsentDTO> updateCitizenConsent(@Valid @RequestBody CitizenConsentDTO citizenConsentDTO);

    /**
     * Send message
     *
     * @param citizenConsentDTO to save
     * @return outcome of sending the save
     */
    @PostMapping("/citizenConsents")
    ResponseEntity<CitizenConsentDTO> saveCitizenConsent(@Valid @RequestBody CitizenConsentDTO citizenConsentDTO);

}

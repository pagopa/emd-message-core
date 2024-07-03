package it.gov.pagopa.message.core.controller;

import it.gov.pagopa.message.core.dto.CitizenConsentDTO;
import it.gov.pagopa.message.core.service.CitizenServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CitizenControllerImpl implements CitizenController{

    private final CitizenServiceImpl citizenService;

    public CitizenControllerImpl(CitizenServiceImpl citizenService) {
        this.citizenService = citizenService;
    }



    @Override
    public ResponseEntity<CitizenConsentDTO> deleteCitizenConsent(CitizenConsentDTO citizenConsentDTO) {
        citizenConsentDTO = citizenService.deleteCitizenConsent(citizenConsentDTO.getHashedFiscalCode(), citizenConsentDTO.getChannelId());
        return new ResponseEntity<>(citizenConsentDTO, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CitizenConsentDTO> updateCitizenConsent(CitizenConsentDTO citizenConsentDTO) {
        citizenConsentDTO = citizenService.updateCitizenConsent(citizenConsentDTO.getHashedFiscalCode(), citizenConsentDTO.getChannelId());
        return new ResponseEntity<>(citizenConsentDTO, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CitizenConsentDTO> saveCitizenConsent(CitizenConsentDTO citizenConsentDTO) {
        citizenConsentDTO = citizenService.createCitizenConsent(citizenConsentDTO);
        return new ResponseEntity<>(citizenConsentDTO, HttpStatus.OK);
    }
}

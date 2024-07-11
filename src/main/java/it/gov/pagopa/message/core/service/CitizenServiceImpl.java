package it.gov.pagopa.message.core.service;


import it.gov.pagopa.common.utils.Utils;
import it.gov.pagopa.message.core.dto.CitizenConsentDTO;
import it.gov.pagopa.message.core.dto.mapper.CitizenConsentMapperObjectToDTO;
import it.gov.pagopa.message.core.exception.custom.UserNotOnboardedException;
import it.gov.pagopa.message.core.model.CitizenConsent;
import it.gov.pagopa.message.core.model.mapper.CitizenConsentMapperDTOToObject;
import it.gov.pagopa.message.core.repository.CitizenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@Slf4j
public class CitizenServiceImpl implements CitizenService{

    private final CitizenRepository citizenRepository;
    private final CitizenConsentMapperObjectToDTO mapperToDTO;
    private final CitizenConsentMapperDTOToObject mapperToObject;

    public CitizenServiceImpl(CitizenRepository citizenRepository, CitizenConsentMapperObjectToDTO mapperToDTO, CitizenConsentMapperDTOToObject mapperToObject) {
        this.citizenRepository = citizenRepository;
        this.mapperToDTO = mapperToDTO;
        this.mapperToObject = mapperToObject;
    }


    @Override
    public CitizenConsentDTO createCitizenConsent(CitizenConsentDTO citizenConsentDTO) {
        log.info("[EMD][CREATE-CITIZEN-CONSENT] Received message: {}",citizenConsentDTO.toString());
        CitizenConsent citizenConsent = mapperToObject.citizenConsentDTOMapper(citizenConsentDTO);
        String hashedFiscalCode = Utils.createSHA256(citizenConsent.getHashedFiscalCode());
        citizenConsent.setHashedFiscalCode(hashedFiscalCode);
        citizenConsent.setCreationDate(LocalDateTime.now());
        citizenConsent.setLastUpdateDate(LocalDateTime.now());
        citizenConsent  = citizenRepository.save(citizenConsent);
        log.info("[EMD][CREATE-CITIZEN-CONSENT] Created");
        return mapperToDTO.citizenConsentMapper(citizenConsent);
    }


    @Override
    public CitizenConsentDTO deleteCitizenConsent(String fiscalCode, String channelId) {
        log.info("[EMD][DELETE-CITIZEN-CONSENT] Received hashedFiscalCode: {} and channelId {} ",fiscalCode, channelId);
        String hashedFiscalCode = Utils.createSHA256(fiscalCode);
        CitizenConsent citizenConsent = citizenRepository.findByHashedFiscalCodeAndChannelId(hashedFiscalCode,channelId);
        if(citizenConsent == null) {
            log.error("[EMD][DELETE-CITIZEN-CONSENT] User not onboarded");
            throw new UserNotOnboardedException("User not onboarded", true, null);
        }
        citizenConsent.setChannelState(false);
        citizenConsent.setLastUpdateDate(LocalDateTime.now());
        citizenRepository.save(citizenConsent);
        log.info("[EMD][DELETE-CITIZEN-CONSENT] Deleted");
        return mapperToDTO.citizenConsentMapper(citizenConsent);
    }

    @Override
    public CitizenConsentDTO updateCitizenConsent(String fiscalCode, String channelId) {
        log.info("[EMD][UPDATE-CITIZEN-CONSENT] Received fiscalCode: {} and channelId {} ",fiscalCode, channelId);
        fiscalCode = Utils.createSHA256(fiscalCode);
        CitizenConsent citizenConsent = citizenRepository.findByHashedFiscalCodeAndChannelId(fiscalCode,channelId);
        if(citizenConsent == null) {
            log.error("[EMD][UPDATE-CITIZEN-CONSENT] User not onboarded");
            throw new UserNotOnboardedException("User not onboarded", true, null);
        }
        citizenConsent.setChannelState(true);
        citizenConsent.setLastUpdateDate(LocalDateTime.now());
        citizenRepository.save(citizenConsent);
        log.info("[EMD][UPDATE-CITIZEN-CONSENT] Updated");
        return mapperToDTO.citizenConsentMapper(citizenConsent);
    }

}

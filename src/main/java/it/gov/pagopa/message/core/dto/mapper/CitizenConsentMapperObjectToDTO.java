package it.gov.pagopa.message.core.dto.mapper;


import it.gov.pagopa.message.core.dto.CitizenConsentDTO;
import it.gov.pagopa.message.core.model.CitizenConsent;
import org.springframework.stereotype.Service;

@Service
public class CitizenConsentMapperObjectToDTO {

    public CitizenConsentDTO citizenConsentMapper(CitizenConsent citizenConsent){
        return CitizenConsentDTO.builder()
                .channelState(citizenConsent.getChannelState())
                .channelId(citizenConsent.getChannelId())
                .hashedFiscalCode(citizenConsent.getHashedFiscalCode())
                .userId(citizenConsent.getUserId())
                .lastUpdateDate(citizenConsent.getLastUpdateDate())
                .creationDate(citizenConsent.getCreationDate())
                .build();
    }
}

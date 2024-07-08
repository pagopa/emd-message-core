package it.gov.pagopa.message.core.model.mapper;


import it.gov.pagopa.message.core.dto.CitizenConsentDTO;
import it.gov.pagopa.message.core.model.CitizenConsent;
import org.springframework.stereotype.Service;

@Service
public class CitizenConsentMapperDTOToObject {

    public CitizenConsent citizenConsentDTOMapper(CitizenConsentDTO citizenConsentDTO){
        return CitizenConsent.builder()
                .channelState(true)
                .channelId(citizenConsentDTO.getChannelId())
                .hashedFiscalCode(citizenConsentDTO.getHashedFiscalCode())
                .userId(citizenConsentDTO.getUserId())
                .creationDate(citizenConsentDTO.getCreationDate())
                .lastUpdateDate(citizenConsentDTO.getLastUpdateDate())
                .build();
    }
}

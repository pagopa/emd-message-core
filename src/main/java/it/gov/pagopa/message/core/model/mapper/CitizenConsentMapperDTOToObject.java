package it.gov.pagopa.message.core.model.mapper;



import it.gov.pagopa.message.core.dto.CitizenConsentDTO;
import it.gov.pagopa.message.core.model.CitizenConsent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CitizenConsentMapperDTOToObject {

    public CitizenConsent citizenConsentDTOMapper(CitizenConsentDTO citizenConsentDTO){
        return CitizenConsent.builder()
                .channelState(true)
                .channelId(citizenConsentDTO.getChannelId())
                .hashedFiscalCode(citizenConsentDTO.getHashedFiscalCode())
                .userId(citizenConsentDTO.getUserId())
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .build();
    }
}

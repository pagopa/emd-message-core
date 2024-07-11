package it.gov.pagopa.message.core.faker;



import it.gov.pagopa.message.core.dto.CitizenConsentDTO;

import java.time.LocalDateTime;

public class CitizenConsentDTOFaker {

    private CitizenConsentDTOFaker(){}
    public static CitizenConsentDTO mockInstance(Boolean bias) {
        return CitizenConsentDTO.builder()
                .channelId("channelId")
                .channelState(bias)
                .userId("userId")
                .hashedFiscalCode("hashedFiscalCode")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .build();
    }
}

package it.gov.pagopa.message.faker;

import it.gov.pagopa.message.dto.CitizenConsentDTO;

public class CitizenConsentDTOFaker {

    private CitizenConsentDTOFaker(){}
    public static CitizenConsentDTO mockInstance(Boolean bias) {
        return CitizenConsentDTO.builder()
                .tppId("channelId")
                .tppState(bias)
                .hashedFiscalCode("hashedFiscalCode")
                .build();

    }
}

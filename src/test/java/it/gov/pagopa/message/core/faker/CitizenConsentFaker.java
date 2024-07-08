package it.gov.pagopa.message.core.faker;


import it.gov.pagopa.message.core.model.CitizenConsent;

import java.time.LocalDateTime;

public class CitizenConsentFaker {

    private CitizenConsentFaker(){}
    public static CitizenConsent mockInstance(Boolean bias) {
        return CitizenConsent.builder()
                .channelId("channelId")
                .channelState(bias)
                .userId("userId")
                .hashedFiscalCode("hashedFiscalCode")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .build();
    }
}

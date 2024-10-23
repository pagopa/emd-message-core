package it.gov.pagopa.message.faker;

import it.gov.pagopa.message.dto.TppDTO;

public class TppDTOFaker {
    private TppDTOFaker(){}
    public static TppDTO mockInstance() {
        return TppDTO.builder()
                .tppId("id")
                .build();
    }
}

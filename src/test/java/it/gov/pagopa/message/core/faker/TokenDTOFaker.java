package it.gov.pagopa.message.core.faker;

import it.gov.pagopa.message.core.dto.TokenDTO;

public class TokenDTOFaker {

    private TokenDTOFaker(){}
    public static TokenDTO mockInstance() {
        return TokenDTO.builder()
                .accessToken("accessToken")
                .expiresIn(1)
                .tokenType("tokenType")
                .build();

    }
}

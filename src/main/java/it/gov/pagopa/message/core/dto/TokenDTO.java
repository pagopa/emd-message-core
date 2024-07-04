package it.gov.pagopa.message.core.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class TokenDTO {
    @JsonAlias("access_token")
    private String accessToken;
    @JsonAlias("token_type")
    private String tokenType;
    @JsonAlias("expires_in")
    private long expiresIn;
}

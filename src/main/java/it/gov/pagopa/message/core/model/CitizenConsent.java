package it.gov.pagopa.message.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "citizen_consents")
@Data
@SuperBuilder
@NoArgsConstructor
public class CitizenConsent {

    @JsonAlias("_id")
    private String id;
    @JsonProperty("hashed_fiscal_code")
    private String hashedFiscalCode;
    @JsonProperty("channel_id")
    private String channelId;
    @JsonProperty("channel_state")
    private Boolean channelState;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("creation_date")
    private LocalDateTime creationDate;
    @JsonProperty("last_update_date")
    private LocalDateTime lastUpdateDate;

}

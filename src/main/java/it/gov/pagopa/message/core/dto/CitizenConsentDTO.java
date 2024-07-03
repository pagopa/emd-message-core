package it.gov.pagopa.message.core.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CitizenConsentDTO {
    @JsonAlias("fiscalCode")
    private String hashedFiscalCode;
    private String channelId;
    private Boolean channelState;
    private String userId;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
}

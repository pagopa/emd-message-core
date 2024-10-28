package it.gov.pagopa.message.model;

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

    private String id;
    private String hashedFiscalCode;
    private String tppId;
    private Boolean tppState;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

}

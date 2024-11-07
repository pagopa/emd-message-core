package it.gov.pagopa.message.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "citizen_consents")
@Data
@SuperBuilder
@NoArgsConstructor
public class CitizenConsent {

    private String id;
    private String fiscalCode;
    private Map<String, ConsentDetails> consents;

}


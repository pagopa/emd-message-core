package it.gov.pagopa.message.core.model;


import it.gov.pagopa.message.core.enums.AuthenticationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "channel")
@Data
@SuperBuilder
@NoArgsConstructor
public class Channel {

    private String id;
    private String entityId;
    private String businessName;
    private String uri;
    private String messageUrl;
    private String authenticationUrl;
    private AuthenticationType authenticationType;
    private Contact contact;
    private Boolean state;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
}
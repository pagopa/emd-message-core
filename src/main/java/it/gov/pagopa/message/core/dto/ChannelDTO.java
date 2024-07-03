package it.gov.pagopa.message.core.dto;


import it.gov.pagopa.message.core.enums.AuthenticationType;
import it.gov.pagopa.message.core.model.Contact;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ChannelDTO {
    private String id;
    private String entityId;
    private String businessName;
    private String uri;
    private String messageUrl;
    private String authenticationUrl;
    private AuthenticationType authenticationType;
    private Contact contact;
    private Boolean state;
}
